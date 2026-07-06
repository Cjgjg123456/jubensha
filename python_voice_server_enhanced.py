# -*- coding: utf-8 -*-
"""
Python 语音转文字中间层服务（增强版）
支持 M4A、MP3 等格式自动转换
使用 ctypes 直接调用 libvosk.dll，无需安装 vosk pip 包
"""

import os
import sys
import io
import json
import wave
import ctypes
import tempfile
import re
from flask import Flask, request, jsonify

# 修复 Windows 控制台编码问题
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# 尝试导入 soundfile 用于处理 WAV 等格式
try:
    import soundfile as sf
    import numpy as np
    SOUNDFILE_AVAILABLE = True
    print("✅ soundfile 库已加载")
except ImportError:
    SOUNDFILE_AVAILABLE = False
    print("⚠️ soundfile 库未安装")

# 尝试导入 pydub 用于处理 WebM 等格式
try:
    from pydub import AudioSegment
    PYDUB_AVAILABLE = True
    print("✅ pydub 库已加载")
except ImportError:
    PYDUB_AVAILABLE = False
    print("⚠️ pydub 库未安装")

app = Flask(__name__)

# 全局变量
_libvosk = None
_model = None
_model_path = "vosk-model-small-cn-0.22"

def load_vosk_library():
    """加载 Vosk 库"""
    global _libvosk
    
    # 查找 libvosk.dll
    dll_paths = [
        "./libvosk.dll",
        "./vosk-win64-0.3.45/libvosk.dll",
        "./jubensha_libs/libvosk.dll",
        "./src/main/resources/native/libvosk.dll"
    ]
    
    dll_path = None
    for path in dll_paths:
        if os.path.exists(path):
            dll_path = path
            break
    
    if dll_path is None:
        print("❌ 未找到 libvosk.dll")
        return False
    
    try:
        print(f"⏳ 正在加载 Vosk 库：{dll_path}")
        
        # 设置 PATH 环境变量，确保依赖库能被找到
        dll_dir = os.path.dirname(os.path.abspath(dll_path))
        os.environ["PATH"] = dll_dir + os.pathsep + os.environ["PATH"]
        
        # 加载库
        _libvosk = ctypes.CDLL(dll_path)
        
        # 设置函数签名
        _libvosk.vosk_model_new.argtypes = [ctypes.c_char_p]
        _libvosk.vosk_model_new.restype = ctypes.c_void_p
        
        _libvosk.vosk_model_free.argtypes = [ctypes.c_void_p]
        
        _libvosk.vosk_recognizer_new.argtypes = [ctypes.c_void_p, ctypes.c_float]
        _libvosk.vosk_recognizer_new.restype = ctypes.c_void_p
        
        _libvosk.vosk_recognizer_free.argtypes = [ctypes.c_void_p]
        
        _libvosk.vosk_recognizer_accept_waveform.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_int]
        _libvosk.vosk_recognizer_accept_waveform.restype = ctypes.c_int
        
        _libvosk.vosk_recognizer_result.argtypes = [ctypes.c_void_p]
        _libvosk.vosk_recognizer_result.restype = ctypes.c_char_p
        
        _libvosk.vosk_recognizer_final_result.argtypes = [ctypes.c_void_p]
        _libvosk.vosk_recognizer_final_result.restype = ctypes.c_char_p
        
        print(f"✅ Vosk 库加载成功")
        return True
        
    except Exception as e:
        print(f"❌ 加载 Vosk 库失败：{e}")
        return False

def load_model():
    """加载语音识别模型"""
    global _model
    
    if not os.path.exists(_model_path):
        print(f"❌ 模型目录不存在：{_model_path}")
        return False
    
    try:
        print(f"⏳ 正在加载模型：{_model_path}")
        
        model_ptr = _libvosk.vosk_model_new(_model_path.encode('utf-8'))
        
        if model_ptr == 0:
            print("❌ 模型加载失败")
            return False
        
        _model = model_ptr
        print(f"✅ 模型加载成功")
        return True
        
    except Exception as e:
        print(f"❌ 加载模型失败：{e}")
        return False

def recognize_audio(audio_bytes, filename="audio.wav"):
    """识别音频数据（支持 WebM、MP4、M4A、MP3、WAV 等格式）"""
    global _model
    
    if _model is None:
        return {'success': False, 'error': '模型未加载'}
    
    try:
        # 根据文件名确定文件扩展名
        ext = os.path.splitext(filename)[1].lower()
        print(f"📋 检测到文件格式: {ext}")
        
        # 写入临时文件（使用原始扩展名）
        temp_file = tempfile.mktemp(suffix=ext)
        with open(temp_file, 'wb') as f:
            f.write(audio_bytes)
        
        # 需要转换的格式列表
        formats_need_conversion = ['.webm', '.mp4', '.m4a', '.mp3', '.ogg', '.flac']
        
        # 如果不是 WAV 格式，先转换
        wav_file = temp_file
        if ext in formats_need_conversion:
            print(f"🔄 需要转换格式 {ext} -> WAV")
            converted_file = convert_with_ffmpeg(temp_file, filename)
            if converted_file and os.path.exists(converted_file):
                wav_file = converted_file
                print(f"✅ 格式转换成功")
            else:
                os.remove(temp_file)
                return {'success': False, 'error': f'无法转换音频格式：{filename}，请确保安装了 FFmpeg'}
        
        # 读取 WAV 文件
        wf = wave.open(wav_file, "rb")
        n_channels = wf.getnchannels()
        sample_width = wf.getsampwidth()
        framerate = wf.getframerate()
        
        # 如果格式不符合要求，进行转换
        if n_channels != 1 or sample_width != 2 or framerate != 16000:
            print(f"⚠️  音频格式不符合要求，正在转换...")
            converted_file = convert_audio_format(temp_file)
            if converted_file:
                wf.close()
                wf = wave.open(converted_file, "rb")
                framerate = 16000
            else:
                wf.close()
                return {'success': False, 'error': '音频格式转换失败'}
        
        # 创建识别器
        rec = _libvosk.vosk_recognizer_new(_model, ctypes.c_float(framerate))
        
        if rec == 0:
            wf.close()
            return {'success': False, 'error': '创建识别器失败'}
        
        # 识别音频
        results = []
        while True:
            data = wf.readframes(4000)
            if len(data) == 0:
                break
            
            res = _libvosk.vosk_recognizer_accept_waveform(rec, data, len(data))
            if res == 1:
                result_ptr = _libvosk.vosk_recognizer_result(rec)
                result_str = ctypes.string_at(result_ptr).decode('utf-8')
                result = json.loads(result_str)
                text = result.get('text', '')
                if text:
                    results.append(text)
        
        # 获取最终结果
        result_ptr = _libvosk.vosk_recognizer_final_result(rec)
        final_result_str = ctypes.string_at(result_ptr).decode('utf-8')
        final_result = json.loads(final_result_str)
        final_text = final_result.get('text', '')
        
        if final_text:
            results.append(final_text)
        
        # 释放识别器
        _libvosk.vosk_recognizer_free(rec)
        wf.close()
        
        # 清理临时文件
        if os.path.exists(temp_file):
            os.remove(temp_file)
        
        # ✅ 彻底清理所有中文之间的空格
        # 方法1：使用最后一个完整结果（最准确）
        full_text = final_text.strip() if final_text else ''
        
        # 如果没有最终结果，使用片段拼接
        if not full_text and results:
            full_text = ''.join(results)
        
        # ✅ 彻底清理所有空格（适用于中文）
        # 1. 去掉所有连续空格
        full_text = re.sub(r'\s+', '', full_text)
        
        # 2. 去掉首尾空格
        full_text = full_text.strip()
        
        return {
            'success': True,
            'text': full_text,
            'confidence': 1.0
        }
        
    except Exception as e:
        print(f"❌ 识别失败：{e}")
        import traceback
        traceback.print_exc()
        return {'success': False, 'error': str(e)}

def convert_with_ffmpeg(input_file, original_filename):
    """使用 pydub 或 ffmpeg 转换音频格式（支持 WebM、MP4、M4A、MP3 等）"""
    # 优先使用 pydub（不需要 ffmpeg 在 PATH 中）
    if PYDUB_AVAILABLE:
        try:
            result = convert_with_pydub(input_file)
            if result:
                return result
        except Exception as e:
            print(f"⚠️ pydub 转换失败: {e}，尝试 soundfile")
    
    # 尝试使用 soundfile
    if SOUNDFILE_AVAILABLE:
        try:
            result = convert_with_soundfile(input_file)
            if result:
                return result
        except Exception as e:
            print(f"⚠️ soundfile 转换失败: {e}")
    
    # 回退到 ffmpeg 命令行工具
    try:
        import subprocess
        
        output_file = tempfile.mktemp(suffix=".wav")
        
        # ffmpeg 命令 - 添加更多参数确保兼容性
        cmd = [
            'ffmpeg',
            '-i', input_file,
            '-ar', '16000',       # 采样率 16kHz
            '-ac', '1',           # 单声道
            '-codec:a', 'pcm_s16le',  # 16-bit PCM
            '-f', 'wav',          # WAV 格式
            '-y',                 # 覆盖输出文件
            '-hide_banner',       # 隐藏横幅
            '-loglevel', 'error', # 只显示错误
            output_file
        ]
        
        print(f"🔄 执行 ffmpeg 转换：{' '.join(cmd)}")
        
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=60
        )
        
        if result.returncode == 0 and os.path.exists(output_file):
            print(f"✅ ffmpeg 转换成功")
            return output_file
        else:
            print(f"❌ ffmpeg 转换失败，退出码: {result.returncode}")
            if result.stderr:
                print(f"   错误信息: {result.stderr[:500]}")
            return None
            
    except FileNotFoundError:
        print(f"❌ ffmpeg 未安装或不在 PATH 中")
        return None
    except Exception as e:
        print(f"❌ ffmpeg 转换错误：{e}")
        return None

def convert_with_soundfile(input_file):
    """使用 soundfile 库转换音频格式（纯 Python，不需要 ffmpeg）"""
    print(f"🔄 使用 soundfile 转换音频...")
    
    output_file = tempfile.mktemp(suffix=".wav")
    
    # 使用 soundfile 读取音频
    data, samplerate = sf.read(input_file)
    
    # 转换为单声道
    if len(data.shape) > 1:
        data = np.mean(data, axis=1)
        print(f"   转换为单声道")
    
    # 重采样到 16kHz
    if samplerate != 16000:
        print(f"   重采样: {samplerate} Hz -> 16000 Hz")
        ratio = 16000 / samplerate
        new_length = int(len(data) * ratio)
        old_indices = np.arange(len(data))
        new_indices = np.linspace(0, len(data) - 1, new_length)
        data = np.interp(new_indices, old_indices, data)
    
    # 转换为 int16
    data_int16 = (data * 32767).astype(np.int16)
    
    # 写入 WAV 文件
    sf.write(output_file, data_int16, 16000, format='WAV', subtype='PCM_16')
    
    print(f"✅ soundfile 转换成功")
    return output_file

def convert_with_pydub(input_file):
    """使用 pydub 库转换音频格式（支持 WebM、Opus 等格式）"""
    print(f"🔄 使用 pydub 转换音频...")
    
    output_file = tempfile.mktemp(suffix=".wav")
    
    # 使用 pydub 加载音频（自动检测格式，支持 WebM/Opus）
    audio = AudioSegment.from_file(input_file)
    
    # 转换为 16kHz, 单声道, 16-bit
    audio = audio.set_frame_rate(16000).set_channels(1).set_sample_width(2)
    
    # 导出为 WAV
    audio.export(output_file, format="wav")
    
    print(f"✅ pydub 转换成功")
    return output_file

def try_alternative_conversion(input_file):
    """尝试使用 pydub 进行音频转换（如果 ffmpeg 不可用）"""
    try:
        from pydub import AudioSegment
        
        output_file = tempfile.mktemp(suffix=".wav")
        
        print(f"🔄 尝试使用 pydub 转换...")
        
        # 使用 pydub 加载音频（自动检测格式）
        audio = AudioSegment.from_file(input_file)
        
        # 转换为 16kHz, 单声道, 16-bit
        audio = audio.set_frame_rate(16000).set_channels(1).set_sample_width(2)
        
        # 导出为 WAV
        audio.export(output_file, format="wav")
        
        print(f"✅ pydub 转换成功")
        return output_file
        
    except ImportError:
        print(f"❌ pydub 未安装")
        return None
    except Exception as e:
        print(f"❌ pydub 转换失败：{e}")
        return None

def convert_audio_format(input_file):
    """转换音频格式为 16kHz, 16-bit, Mono"""
    try:
        import numpy as np
        
        output_file = tempfile.mktemp(suffix=".wav")
        
        with wave.open(input_file, 'rb') as wav_in:
            n_channels = wav_in.getnchannels()
            sample_width = wav_in.getsampwidth()
            framerate = wav_in.getframerate()
            n_frames = wav_in.getnframes()
            
            audio_data = wav_in.readframes(n_frames)
            
            if sample_width == 2:
                audio_array = np.frombuffer(audio_data, dtype=np.int16)
            else:
                audio_array = np.frombuffer(audio_data, dtype=np.int32)
            
            # 转换为浮点数
            audio_float = audio_array.astype(np.float32) / (2 ** (8 * sample_width - 1))
            
            # 如果是立体声，转换为单声道
            if n_channels > 1:
                audio_float = audio_float.reshape(-1, n_channels)
                audio_float = np.mean(audio_float, axis=1)
            
            # 重采样到 16kHz
            if framerate != 16000:
                ratio = 16000 / framerate
                new_length = int(len(audio_float) * ratio)
                old_indices = np.arange(len(audio_float))
                new_indices = np.linspace(0, len(audio_float) - 1, new_length)
                audio_float = np.interp(new_indices, old_indices, audio_float)
            
            # 转换回 16-bit 整数
            audio_int16 = (audio_float * 32767).astype(np.int16)
            
            with wave.open(output_file, 'wb') as wav_out:
                wav_out.setnchannels(1)
                wav_out.setsampwidth(2)
                wav_out.setframerate(16000)
                wav_out.writeframes(audio_int16.tobytes())
        
        return output_file
        
    except Exception as e:
        print(f"❌ 音频格式转换失败：{e}")
        return None

@app.route('/api/recognize', methods=['POST'])
def api_recognize():
    """语音识别 API"""
    if 'file' not in request.files:
        return jsonify({'success': False, 'error': '未上传文件'}), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify({'success': False, 'error': '文件名为空'}), 400
    
    try:
        # 读取文件内容
        audio_bytes = file.read()
        
        if len(audio_bytes) == 0:
            return jsonify({'success': False, 'error': '文件内容为空'}), 400
        
        print(f"📥 收到音频文件：{file.filename}, 大小：{len(audio_bytes)} bytes")
        
        # 识别音频
        result = recognize_audio(audio_bytes, file.filename)
        
        print(f"📤 识别结果：{result}")
        
        return jsonify(result)
        
    except Exception as e:
        print(f"❌ API 处理失败：{e}")
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/status', methods=['GET'])
def api_status():
    """服务状态 API"""
    return jsonify({
        'success': True,
        'serviceAvailable': True,
        'modelLoaded': _model is not None,
        'modelPath': _model_path
    })

@app.route('/api/health', methods=['GET'])
def api_health():
    """健康检查 API"""
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    # 加载 Vosk 库
    if not load_vosk_library():
        sys.exit(1)
    
    # 加载模型
    if not load_model():
        sys.exit(1)
    
    print("\n🚀 Python 语音转文字服务启动（增强版）")
    print("📍 服务地址：http://localhost:5000")
    print("📡 API 端点：POST /api/recognize")
    print("🔍 健康检查：GET /api/health")
    print("📊 状态查询：GET /api/status")
    print("🎵 支持格式：WAV, M4A, MP3 (需要 ffmpeg)")
    print("\n按 Ctrl+C 停止服务")
    print("=" * 60)
    
    # 启动服务
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)
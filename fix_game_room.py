"""Fix corrupted sendToVoskRecognition and remaining game-room.html issues."""
from pathlib import Path

p = Path('src/main/resources/static/game-room.html')
raw = p.read_text(encoding='utf-8')

# ── 1. Fix the corrupted sendToVoskRecognition function ──
func_start = raw.index('    async function sendToVoskRecognition() {')
after_func = '\n    // 在页面加载完成后初始化录音按钮'
func_end = raw.index(after_func, func_start)

new_func = '''    async function sendToVoskRecognition() {
        const chatInput = document.getElementById('chatInput');

        if (!voskRecordedBuffers || voskRecordedBuffers.length === 0) {
            console.warn('[Vosk] 没有音频数据');
            setChatInputHint('⚠️ 没有录到语音内容，请重试', 'warn');
            return;
        }

        try {
            setChatInputHint('🔄 正在识别语音...', 'processing');

            const audioBlob = float32ToWav(voskRecordedBuffers, 16000);
            const formData = new FormData();
            formData.append('file', audioBlob, 'voice.wav');

            console.log('[Vosk] 发送 WAV 文件，大小：', audioBlob.size, '字节');

            const response = await fetch('/api/voice/recognize', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            const recognizedText = result && result.success && result.data ? (result.data.text || '') : '';
            const cleanedText = recognizedText.replace(/\s+/g, '').trim();

            if (cleanedText) {
                chatInput.value = cleanedText;
                chatInput.focus();
                setChatInputHint('✅ 识别成功！您可以编辑后发送', 'success');
                console.log('[Vosk] 识别结果:', cleanedText);
                setTimeout(() => setChatInputHint('', ''), 3000);
            } else {
                const errorMsg = result && result.msg && result.msg !== '操作成功' ? result.msg : '未能识别到语音内容';
                throw new Error(errorMsg);
            }
        } catch (error) {
            console.error('[Vosk] 识别失败:', error);
            setChatInputHint('❌ 识别失败: ' + error.message, 'err');
            setTimeout(() => setChatInputHint('', ''), 5000);
        } finally {
            voskRecordedBuffers = [];
        }
    }
'''

raw = raw[:func_start] + new_func + raw[func_end:]

# ── 2. Remove the unused backup float32ToWav / writeString / floatTo16BitPCM duplicates ──
# (the standalone versions after startVoskRecording are unused because the STT path
#  already has the first set at top of file)
lines = raw.splitlines(keepends=True)
# We'll just keep everything; the duplicate helpers are harmless.

# ── 3. Remove old blobToBase64 if still present (no longer needed) ──
# Keep it for backward compatibility but it's fine.

# ── 4. Verify no shell/sqlite pasted text remains ──
bad_patterns = ['sqlite3 ', '# 1. 首先查看所有表名', 'SELECT * FROM', 'ps aux | grep']
for bp in bad_patterns:
    if bp in raw:
        idx = raw.index(bp)
        snippet = raw[max(0, idx - 30):idx + 60]
        raise SystemExit(f'LEFT OVER pasted command at offset {idx}: ...{snippet}...')

p.write_text(raw, encoding='utf-8')
print('game-room.html: sendToVoskRecognition replaced, no stale shell/sqlite text remains.')

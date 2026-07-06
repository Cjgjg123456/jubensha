const VoiceRecognitionUtil = {
    isSupported: () => {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        return typeof SpeechRecognition !== 'undefined' &&
               typeof navigator !== 'undefined' &&
               typeof navigator.mediaDevices !== 'undefined';
    },

    async requestPermission() {
        try {
            await navigator.mediaDevices.getUserMedia({ audio: true });
            return true;
        } catch (error) {
            console.error('[语音识别] 麦克风权限被拒绝:', error);
            return false;
        }
    }
};

if (typeof module !== 'undefined' && module.exports) {
    module.exports = { VoiceRecognitionUtil };
}
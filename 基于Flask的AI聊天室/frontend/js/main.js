document.addEventListener('DOMContentLoaded', () => {
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const chatMessages = document.getElementById('chat-messages');
    const roleSelect = document.getElementById('role-select');

    const addMessage = (content, isUser) => {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isUser ? 'user-message' : 'ai-message'}`;
        messageDiv.textContent = content;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    };

    const addLoadingMessage = () => {
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'message ai-message';
        loadingDiv.textContent = 'AI正在输入...';
        loadingDiv.id = 'loading-message';
        chatMessages.appendChild(loadingDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    };
    
    const removeLoadingMessage = () => {
        const loadingDiv = document.getElementById('loading-message');
        if (loadingDiv) {
            loadingDiv.remove();
        }
    };
    
    const toggleSendButton = (enable) => {
        sendButton.disabled = !enable;
        sendButton.style.cursor = enable ? 'pointer' : 'not-allowed';
    };
    
    const sendMessage = async () => {
        const message = messageInput.value.trim();
        if (!message) return;
    
        addMessage(message, true);
        messageInput.value = '';
    
        toggleSendButton(false); // 禁用按钮
        addLoadingMessage();
    
        try {
            const response = await fetch('http://localhost:5000/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: message,
                    role: roleSelect.value
                })
            });
    
            const data = await response.json();
            removeLoadingMessage();
            addMessage(data.response, false);
        } catch (error) {
            console.error('Error:', error);
            removeLoadingMessage();
            addMessage('抱歉，发生了错误，请稍后重试。', false);
        } finally {
            toggleSendButton(true); // 启用按钮
        }
    };

    
    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
});

const saveChatHistory = () => {
    const messages = Array.from(chatMessages.children).map(msg => ({
        content: msg.textContent,
        sender: msg.classList.contains('user-message') ? 'user' : 'ai'
    }));
    localStorage.setItem('chatHistory', JSON.stringify(messages));
};

const loadChatHistory = () => {
    const savedMessages = JSON.parse(localStorage.getItem('chatHistory')) || [];
    savedMessages.forEach(msg => addMessage(msg.content, msg.sender === 'user'));
};

document.addEventListener('DOMContentLoaded', () => {
    loadChatHistory();
    sendButton.addEventListener('click', () => {
        sendMessage();
        saveChatHistory();
    });
});
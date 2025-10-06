const API_BASE = 'http://localhost:8080/api';

const auth = {
    getToken() {
        return localStorage.getItem('jwtToken');
    },

    setToken(token) {
        localStorage.setItem('jwtToken', token);
    },

    removeToken() {
        localStorage.removeItem('jwtToken');
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    getUsername() {
        return localStorage.getItem('username');
    },

    setUsername(username) {
        localStorage.setItem('username', username);
    }
};

const api = {
    async request(url, options = {}) {
        const token = auth.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        console.log(`Making request to: ${API_BASE}${url}`, options);

        try {
            const response = await fetch(`${API_BASE}${url}`, {
                ...options,
                headers
            });

            console.log('Response status:', response.status);

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Response error:', errorText);
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }

            const result = await response.json();
            console.log('Response success:', result);
            return result;

        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    },

    async register(username, password) {
        return this.request('/auth/registration', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
    },

    async login(username, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
    },

    async yandexLogin(code) {
        return this.request('/auth/yandex/login', {
            method: 'POST',
            body: JSON.stringify({ code })
        });
    },

    async sendMessage(message) {
        return this.request('/chat/send', {
            method: 'POST',
            body: JSON.stringify({ message })
        });
    },
    async getMessages(username) {
        const url = `http://localhost:8080/api/llm/responses?username=${encodeURIComponent(username)}`;
        const token = auth.getToken();
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = `Bearer ${token}`;
        const response = await fetch(url, { headers });
        return await response.json();
    }
};


function showMessage(text, type = 'success') {
    const messageEl = document.getElementById('message');
    if (messageEl) {
        messageEl.textContent = text;
        messageEl.className = `message ${type}`;
        messageEl.style.display = 'block';

        setTimeout(() => {
            messageEl.style.display = 'none';
        }, 5000);
    }
    console.log(`${type.toUpperCase()}: ${text}`);
}

function handleYandexCallback() {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');

    if (code) {
        console.log('Yandex callback detected with code:', code);
        processYandexAuthorization(code);
        return true;
    }
    return false;
}

async function processYandexAuthorization(code) {
    try {
        console.log('Processing Yandex authorization with code:', code);
        const result = await api.yandexLogin(code);
        console.log('Yandex auth response:', result);

        if (result.success && result['jwt-token']) {
            auth.setToken(result['jwt-token']);
            auth.setUsername(result.username);
            showMessage(result.message);
            console.log('Yandex auth successful, redirecting to chat...');

            window.history.replaceState({}, document.title, window.location.pathname);

            setTimeout(() => location.href = 'chat.html', 1000);
        } else {
            const errorMsg = result.message || 'Ошибка авторизации через Яндекс';
            showMessage(errorMsg, 'error');
            console.log('Yandex auth failed:', errorMsg);
        }

    } catch (error) {
        console.error('Yandex auth error:', error);
        showMessage('Ошибка авторизации через Яндекс: ' + error.message, 'error');

        setTimeout(() => location.href = 'auth.html', 7000);
    }
}

function loginWithYandex() {
    console.log('Redirecting to Yandex OAuth...');
    window.location.href = `${API_BASE}/auth/yandex`;
}

async function handleRegister(event) {
    console.log("=== handleRegister called ===");

    if (event) {
        event.preventDefault();
        console.log("Event prevented");
    }

    const username = document.getElementById('regUsername')?.value;
    const password = document.getElementById('regPassword')?.value;

    console.log('Registration data:', { username, password });

    if (!username || !password) {
        showMessage('Заполните все поля', 'error');
        console.log('Validation failed: empty fields');
        return;
    }

    try {
        console.log('Sending registration request...');
        const result = await api.register(username, password);
        console.log('Registration response:', result);

        if (result['jwt-token']) {
            auth.setToken(result['jwt-token']);
            auth.setUsername(username);
            showMessage('Регистрация успешна!');
            console.log('Registration successful, redirecting...');
            setTimeout(() => location.href = 'chat.html', 1000);
        } else {
            const errorMsg = result.message || 'Ошибка регистрации: токен не получен';
            showMessage(errorMsg, 'error');
            console.log('Registration failed:', errorMsg);
        }

    } catch (error) {
        console.error('Registration error:', error);
        console.error('Error stack:', error.stack);
        showMessage('Ошибка регистрации: ' + error.message, 'error');
    }
}

async function handleLogin(event) {
    console.log("=== handleLogin called ===");

    if (event) {
        event.preventDefault();
        console.log("Event prevented");
    }

    const username = document.getElementById('loginUsername')?.value;
    const password = document.getElementById('loginPassword')?.value;

    console.log('Login data:', { username, password });

    if (!username || !password) {
        showMessage('Заполните все поля', 'error');
        console.log('Validation failed: empty fields');
        return;
    }

    try {
        console.log('Sending login request...');
        const result = await api.login(username, password);
        console.log('Login response:', result);

        if (result.success) {
            auth.setToken(result['jwt-token']);
            auth.setUsername(result.username);
            showMessage(result.message);
            console.log('Login successful, redirecting...');
            setTimeout(() => location.href = 'chat.html', 1000);
        } else {
            showMessage(result.message, 'error');
            console.log('Login failed:', result.message);
        }

    } catch (error) {
        console.error('Login error:', error);
        console.error('Error stack:', error.stack);
        showMessage('Ошибка входа: ' + error.message, 'error');
    }
}

function handleRegisterButton() {
    handleRegister(null);
}

function handleLoginButton() {
    handleLogin(null);
}

function logout() {
    auth.removeToken();
    localStorage.removeItem('username');
    location.href = 'index.html';
}

window.addEventListener('beforeunload', function() {
    if (messageInterval) {
        clearInterval(messageInterval);
    }
});

// Функции чата
let messageInterval;
let chatHistory = [];

async function sendMessage() {
    const input = document.getElementById('messageInput');
    const message = input.value.trim();

    if (!message) return;

    const timestamp = new Date().toLocaleTimeString();

    chatHistory.push({ type: 'user', text: message, time: timestamp });

    chatHistory.push({ type: 'loading', text: 'LLM генерирует ответ...', time: '' });

    renderChat();
    input.value = '';

    try {
        await api.sendMessage(message);
    } catch (error) {
        console.error('Send message error:', error);
        showMessage('Ошибка отправки сообщения: ' + error.message, 'error');
    }
}


async function loadMessages() {
    try {
        const username = auth.getUsername();
        const responses = await api.getMessages(username);

        chatHistory = chatHistory.filter(msg => msg.type !== 'loading');

        responses.forEach(response => {
            const exists = chatHistory.some(msg => msg.type === 'llm' && msg.text === response);
            if (!exists) {
                chatHistory.push({
                    type: 'llm',
                    text: response,
                    time: new Date().toLocaleTimeString()
                });
            }
        });

        renderChat();
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

function renderChat() {
    const messagesEl = document.getElementById('messages');
    if (!messagesEl) return;

    const isAtBottom = messagesEl.scrollHeight - messagesEl.scrollTop <= messagesEl.clientHeight + 50;

    messagesEl.innerHTML = chatHistory.map(msg => {
        let className = 'message-item';
        let sender = '';

        if (msg.type === 'user') {
            className += ' user';
            sender = 'Вы';
        } else if (msg.type === 'llm') {
            className += ' llm';
            sender = 'LLM';
        } else if (msg.type === 'loading') {
            className += ' loading';
        }

        const time = msg.time ? `<small>${msg.time}</small>` : '';

        return `
            <div class="${className}">
                ${sender ? `<strong>${sender}:</strong>` : ''}
                ${escapeHTML(msg.text)}
                ${time}
            </div>
        `;
    }).join('');

    if (isAtBottom) {
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }
}


function escapeHTML(text) {
    const div = document.createElement('div');
    div.innerText = text;
    return div.innerHTML;
}

document.addEventListener('DOMContentLoaded', function () {
    console.log('Page loaded, auth status:', auth.isLoggedIn());

    const isProcessingYandex = handleYandexCallback();
    if (isProcessingYandex) return;

    if (auth.isLoggedIn()) {
        const userInfoEl = document.getElementById('userInfo');
        const chatUsernameEl = document.getElementById('chatUsername');
        const username = auth.getUsername();

        if (userInfoEl) {
            document.getElementById('username').textContent = username;
            userInfoEl.style.display = 'block';
        }

        if (chatUsernameEl) {
            chatUsernameEl.textContent = username;
        }
    }

    if (window.location.pathname.endsWith('/chat.html')) {
        if (!auth.isLoggedIn()) {
            location.href = '/auth.html';
            return;
        }

        loadMessages();
        messageInterval = setInterval(loadMessages, 3000);

        const messageInput = document.getElementById('messageInput');
        if (messageInput) {
            messageInput.addEventListener('keypress', function (e) {
                if (e.key === 'Enter') sendMessage();
            });
        }
    }
});

window.addEventListener('beforeunload', function () {
    if (messageInterval) {
        clearInterval(messageInterval);
    }
});

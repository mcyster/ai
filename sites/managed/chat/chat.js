// Define ErrorManager early to ensure it catches all errors
const ErrorManager = {
    errors: [],
    addError(error) {
        const message = error.message || 'Unknown error';
        const stack = error.stack || 'No stack trace available';
        const errorDetails = `${message}\n${stack}`;
        this.errors.push(errorDetails);
        this.updateButtonVisibility();
    },
    getError() {
        return this.errors.length > 0 ? this.errors[0] : null;
    },
    removeError() {
        if (this.errors.length > 0) {
            this.errors.shift();
        }
        this.updateButtonVisibility();
    },
    updateButtonVisibility() {
        const sendErrorsButton = document.getElementById('sendErrorsButton');
        if (sendErrorsButton) {
            sendErrorsButton.style.display = this.errors.length > 0 ? 'inline-block' : 'none';
        }
    }
};

// Global error handlers
window.addEventListener('error', function (event) {
    const file = event.filename || 'Not available';
    const line = event.lineno || 'Not available';
    const column = event.colno || 'Not available';
    const error = {
        message: event.message,
        stack: `File: ${file}\nLine: ${line}\nColumn: ${column}`
    };
    ErrorManager.addError(error);
});

window.addEventListener('unhandledrejection', function (event) {
    const reason = event.reason instanceof Error ? event.reason : { message: event.reason, stack: 'No stack trace available' };
    const error = {
        message: reason.message,
        stack: `Stack: ${reason.stack}`
    };
    ErrorManager.addError(error);
});

// Function to dynamically load a JavaScript file
function loadScript(url, callback) {
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;
    script.onload = callback;
    script.onerror = function (e) { 
        console.error('Error loading script:', e);
        ErrorManager.addError({ message: `Failed to load script: ${url}`, stack: e.message });
    };
    document.head.appendChild(script);
}

// Function to ensure Vue.js is loaded
function ensureVue(callback) {
    if (typeof Vue === 'undefined') {
        loadScript('https://cdn.jsdelivr.net/npm/vue@3', callback);
    } else {
        callback();
    }
}

// Ensure marked.min.js is properly available
function ensureMarked(callback) {
    if (typeof marked === 'undefined') {
        loadScript('https://cdn.jsdelivr.net/npm/marked/marked.min.js', callback);
    } else {
        callback();
    }
}

// Main initialization function for the app
function initializeApp() {
    const { createApp, nextTick } = Vue;

    function getQueryParams() {
        const params = new URLSearchParams(window.location.search);
        let queryParams = {};
        for (const [key, value] of params.entries()) {
            queryParams[key] = value;
        }
        return queryParams;
    }

    function findCurrentScript() {
        var scripts = document.getElementsByTagName('script');
        for (var i = 0; i < scripts.length; i++) {
            if (scripts[i].src.includes('chat.js')) {
                return scripts[i];
            }
        }
        return null;
    }

    function getScriptTagParams() {
        var currentScript = document.currentScript;
        if (!currentScript) {
            currentScript = findCurrentScript();
        }

        if (currentScript == null) {
            console.error("Error unable to identify current script", currentScript);
            return null;
        }

        var parameters = {};
        for (var i = 0; i < currentScript.attributes.length; i++) {
            var attr = currentScript.attributes[i];
            if (attr.name.startsWith('data-')) {
                var name = attr.name.slice(5);
                parameters[name] = attr.value;
            }
        }

        return parameters;
    }

    function capitalizeTagParameter(input) {
        return input.split('-').map((word, index) => {
            if (index === 0) {
                return word;
            }
            return word.charAt(0).toUpperCase() + word.slice(1);
        }).join('');
    }

    function injectStyles() {
        const styles = `
            #chatOverlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                display: none; /* Initially hidden */
            }
            #chatApp {
                width: 100%;
                max-width: 600px;
                max-height: 90vh;
                background: white;
                padding: 20px;
                box-shadow: 0 0 10px rgba(0,0,0,0.1);
                border-radius: 5px;
                overflow-y: auto; /* Add vertical scroll if content overflows */
                position: relative;
            }
            #openOverlayButton {
                position: fixed;
                bottom: 20px;
                right: 20px;
                padding: 10px 15px;
                border: none;
                background-color: rgba(0, 123, 255, 0.8); /* Blue with partial transparency */
                color: white;
                border-radius: 5px;
                cursor: pointer;
            }
            .overlayButtonContainer {
                position: sticky;
                top: 0;
                display: flex;
                justify-content: flex-end;
                background: white;
                z-index: 1;
            }
            .closeOverlayButton, .reloadOverlayButton, .sendErrorsButton {
                padding: 5px 10px;
                border: none;
                border-radius: 5px;
                margin: 5px;
                cursor: pointer;
            }
            .closeOverlayButton {
                background-color: #ff0000;
                color: white;
            }
            .reloadOverlayButton {
                background-color: #28a745;
                color: white;
                display: none;
            }
            .sendErrorsButton {
                background-color: #ffc107;
                color: white;
                display: none;
            }
            .message {
                padding: 10px;
                border-bottom: 1px solid #e0e0e0;
                word-wrap: break-word;
            }
            .message:last-child {
                border-bottom: none;
            }
            .input-group {
                display: flex;
                margin-top: 10px;
                align-items: center;
            }
            .input-group textarea {
                flex: 1;
                padding: 10px;
                border: 1px solid #e0e0e0;
                border-radius: 5px;
                margin-right: 5px;
                height: 60px;
            }
            .input-group button {
                padding: 10px 15px;
                border: none;
                background-color: #007bff;
                color: white;
                border-radius: 5px;
                cursor: pointer;
                margin-right: 5px;
            }
        `;

        const styleSheet = document.createElement('style');
        styleSheet.type = 'text/css';
        styleSheet.innerText = styles;
        document.head.appendChild(styleSheet);
    }

    function addOverlayHTML() {
        const overlayHTML = `
            <div id="chatOverlay">
                <div id="chatApp">
                    <div class="overlayButtonContainer">
                        <button class="sendErrorsButton" id="sendErrorsButton" @click="sendErrors">Errors</button>
                        <button class="reloadOverlayButton" @click="reloadPage">Reload</button>
                        <button class="closeOverlayButton" @click="closeOverlay">Close</button>
                    </div>
                    <div v-for="message in messages" class="message" v-html="message.html"></div>
                    <div class="input-group" ref="inputGroup">
                        <textarea v-model="newMessage" @keydown="handleKeydown" placeholder="Type a message (2 minutes to process)"></textarea>
                        <button @click="sendMessage">Send</button>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', overlayHTML);
    }

    function addOpenButton() {
        const openButtonHTML = `
            <button id="openOverlayButton">Open Chat</button>
        `;

        document.body.insertAdjacentHTML('beforeend', openButtonHTML);
    }

    function initialize() {
        injectStyles();
        addOpenButton();
        addOverlayHTML();

        const app = createApp({
            data() {
                return {
                    messages: [],
                    newMessage: '',
                    scenario: '',
                    parameters: {},
                    conversationId: null
                };
            },
            mounted() {
                ErrorManager.updateButtonVisibility();
            },
            async created() {
                const scriptTagParams = getScriptTagParams();
                const queryParams = getQueryParams();
                const allParameters = { ...scriptTagParams, ...queryParams };

                this.scenario = allParameters['scenario'] || 'chat';

                var parameters = {}
                for (const key in allParameters) {
                    if (allParameters.hasOwnProperty(key)) {
                        let name = undefined;
                        if (key.startsWith('href-')) {
                            name = capitalizeTagParameter(key.substring(5));
                        } else if (key.startsWith('href.')) {
                            name = key.substring(5);
                        }
                        if (name) {
                            const regex = new RegExp(allParameters[key]);
                            const match = window.location.href.match(regex);
                            if (match) {
                                parameters[name] = match[1];
                            }
                        }
                        name = undefined;
                        if (key.startsWith('parameter-')) {
                            name = capitalizeTagParameter(key.substring(10));
                        } else if (key.startsWith('parameter.')) {
                            name = key.substring(10);
                        }
                        if (name) {
                            parameters[name] = allParameters[key];
                        }
                    }
                }
                this.parameters = parameters;

                const openChat = allParameters['openChat'];

                if (openChat) {
                    this.openOverlay();
                }

                const existingConversationId = allParameters['conversationId'];
                if (existingConversationId) {
                    await this.loadExistingConversation(existingConversationId);
                }

                console.log('Scenario', this.scenario, parameters);
            },
            methods: {
                async createConversation(scenario, parameters) {
                    const response = await fetch('/conversations', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ scenario, parameters })
                    });
                    const data = await response.json();
                    this.conversationId = data.id;
                    return data;
                },
                async loadExistingConversation(existingConversationId) {
                    const response = await fetch(`/conversations/${existingConversationId}`);
                    const data = await response.json();
                    if (data.hasOwnProperty('id')) {
                        this.conversationId = data.id;
                        for (var message of data.messages) {
                            this.messages.push({ text: message.content, html: marked.parse(message.content) });
                        }
                        this.toggleReloadButtonVisibility(true);
                    }
                    return data;
                },
                async loadOrStartConversation() {
                    try {
                        if (!this.conversationId) {
                            await this.createConversation(this.scenario, this.parameters);
                        }
                    } catch (error) {
                        console.error('Unable to get or create a conversation:', error);
                        this.messages.push({ text: 'Error: Could not send message', html: 'Error: Could not send message' });
                        this.toggleReloadButtonVisibility(false);
                    }
                    this.toggleReloadButtonVisibility(true);
                },
                toggleReloadButtonVisibility(isVisible) {
                    const reloadButton = document.querySelector('.reloadOverlayButton');
                    reloadButton.style.display = isVisible ? 'inline-block' : 'none';
                },
                async addMessageToConversation(prompt) {
                    const response = await fetch(`/conversations/${this.conversationId}/messages`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ prompt })
                    });
                    const data = await response.json();
                    return data;
                },
                async sendMessage() {
                    if (this.newMessage.trim() !== '') {
                        // Add the new message to the chat
                        this.messages.push({ text: this.newMessage, html: marked.parse(this.newMessage) });
            
                        // Store the current message
                        const messageToSend = this.newMessage;
            
                        // Clear the input field
                        this.newMessage = '';
            
                        await nextTick();
                        this.scrollToBottom();
            
                        try {
                            await this.loadOrStartConversation();
            
                            // Add message to the created or found conversation
                            const messageResponse = await this.addMessageToConversation(messageToSend);
                            this.messages.push({ text: messageResponse.content, html: marked.parse(messageResponse.content) });
            
                            await nextTick();
                            this.scrollToBottom();
                        } catch (error) {
                            console.error('Error sending message:', error);
                            this.messages.push({ text: 'Error: Could not send message', html: 'Error: Could not send message' });
            
                            await nextTick();
                            this.scrollToBottom();
                        }
                    }
                },
                sendErrors() {
                    const errorToSend = ErrorManager.getError();
                    console.log("Errors", errorToSend);
                    if (errorToSend) {
                        this.newMessage = `JavaScript Error:\n${errorToSend}`;
                        this.scrollToBottom();
                        ErrorManager.removeError();
                    }
                },
                handleKeydown(event) {
                    if (event.key === 'Enter') {
                        if (event.shiftKey || event.metaKey) {
                            this.newMessage += '\n';
                        } else {
                            this.sendMessage();
                        }
                        event.preventDefault();
                    }
                },
                closeOverlay() {
                    document.getElementById('chatOverlay').style.display = 'none';
                },
                openOverlay() {
                    document.getElementById('chatOverlay').style.display = 'flex';
                },
                reloadPage() {
                    const currentUrl = window.location.href.split('?')[0];
                    const newUrl = `${currentUrl}?conversationId=${this.conversationId}&openChat=true`;
                    window.location.href = newUrl;
                },
                scrollToBottom() {
                    const container = this.$refs.inputGroup;
                    container.scrollIntoView({ behavior: 'smooth' });
                }
            }

        });

        document.getElementById('openOverlayButton').addEventListener('click', function () {
            document.getElementById('chatOverlay').style.display = 'flex';
        });

        app.mount('#chatApp');
    }

    // Check if DOM is already loaded, otherwise add event listener
    if (document.readyState === 'loading') {
        window.addEventListener('DOMContentLoaded', (event) => {
            initialize();
        });
    } else {
        initialize();
    }
}

// Ensure Vue.js is loaded before initializing marked and the app
ensureVue(() => ensureMarked(initializeApp));

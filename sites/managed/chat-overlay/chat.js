const { createApp, nextTick } = Vue;

function getQueryParams() {
    const params = new URLSearchParams(window.location.search);
    let queryParams = {};
    for (const [key, value] of params.entries()) {
        queryParams[key] = value;
    }
    return queryParams;
}

function injectStyles() {
    const styles = `
        #overlay {
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
        #app {
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
        .closeOverlayButton {
            position: absolute;
            top: 10px;
            right: 10px;
            padding: 5px 10px;
            border: none;
            background-color: #ff0000;
            color: white;
            border-radius: 5px;
            cursor: pointer;
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
        <div id="overlay">
            <div id="app">
                <button class="closeOverlayButton" @click="closeOverlay">Close</button>
                <div v-for="message in messages" class="message" v-html="message.html"></div>
                <div class="input-group" ref="inputGroup">
                    <textarea v-model="newMessage" @keydown="handleKeydown" placeholder="Type a message"></textarea>
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
        created() {
            const queryParams = getQueryParams();
            this.scenario = queryParams.scenario || 'chat';
            this.parameters = queryParams;
            delete this.parameters.scenario;
        },
        methods: {
            async createConversation(scenario, parameters) {
                const response = await fetch('http://localhost:8080/conversations', {
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
            async addMessageToConversation(prompt) {
                const response = await fetch(`http://localhost:8080/conversations/${this.conversationId}/messages`, {
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
                        // If there is no conversation ID, create a new conversation
                        if (!this.conversationId) {
                            await this.createConversation(this.scenario, this.parameters);
                        }

                        // Add message to the created conversation
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
            handleKeydown(event) {
                if (event.key === 'Enter') {
                    if (event.ctrlKey || event.metaKey) {
                        this.newMessage += '\n';
                    } else {
                        this.sendMessage();
                    }
                    event.preventDefault();
                }
            },
            closeOverlay() {
                document.getElementById('overlay').style.display = 'none';
            },
            scrollToBottom() {
                const container = this.$refs.inputGroup;
                container.scrollIntoView({ behavior: 'smooth' });
            }
        }
    });

    document.getElementById('openOverlayButton').addEventListener('click', function() {
        document.getElementById('overlay').style.display = 'flex';
    });

    app.mount('#app');
}

// Wait for the DOM to fully load before running any scripts
window.addEventListener('DOMContentLoaded', (event) => {
    initialize();
});
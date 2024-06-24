
function getExtoleToken() {
    return new Promise((resolve, reject) => {
        let token = localStorage.getItem('extole_token');
        
        if (!token) {
            token = prompt("Please enter your Extole token:");
            if (token) {
                localStorage.setItem('extole_token', token);
            } else {
                return reject(new Error('Authorization token not provided'));
            }
        }

        validateToken(token)
            .then(isValid => {
                if (isValid) {
                    resolve(token);
                } else {
                    localStorage.removeItem('extole_token');
                    reject(new Error('Invalid token. Please try again.'));
                }
            })
            .catch(error => {
                reject(error);
            });
    });
}

function validateToken(token) {
    const endpoint = `https://api.extole.io/v4/tokens/${token}`;

    return fetch(endpoint, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (response.status === 403) {
            return false;
        }
        return response.json().then(data => {
            console.log('Token is valid. Details:', data);
            return true;
        });
    })
    .catch(error => {
        console.error('Error validating token:', error);
        return false;
    });
}


function getReportId() {
    const urlParams = new URLSearchParams(window.location.search);
    reportId = urlParams.get('report_id');
    if (!reportId) {
        alert("Error: No report_id parameter");
        return Promise.reject('No report_id parameter');
    }
    return reportId;
}

function getData() {
    return new Promise((resolve, reject) => {
        const reportId = getReportId();
        const url = `https://api.extole.io/v4/reports/${reportId}/download.json`;

        getExtoleToken()
            .then(token => {
                if (!token) {
                    alert("Error: No token");
                    return reject('Authorization token not found');
                }

                return fetch(url, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
            })
            .then(response => {
                if (!response.ok) {
                    alert("Error: unable to fetch " + url);
                    return reject(new Error('Error fetching "' + url + '": ' + response.statusText));
                }
                return response.json();
            })
            .then(data => {
                console.log("got data", data);
                resolve(data);
            })
            .catch(error => {
                alert("Error: problem fetching " + url);
                reject(error);
            });
    });
}


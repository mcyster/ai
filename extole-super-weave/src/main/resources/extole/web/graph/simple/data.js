
function getExtoleToken() {
    let token = localStorage.getItem('extole_token');
    if (!token) {
        token = prompt("Please enter your Extole token:");
        if (token) {
            localStorage.setItem('extole_token', token);
        } else {
            throw new Error('Authorization token not provided');
        }
    }
    return token;
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
    const reportId = getReportId();
    const url = `https://api.extole.io/v4/reports/${reportId}/download.json`

    const token = getExtoleToken();
    if (!token) {
        alert("Error: No token");
        return Promise.reject('Authorization token not found');
    }

    return fetch(url,  {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        if (!response.ok) {
            alert("Error: unable to fetch " + url);
            throw new Error('Error fetching "' + url + '": ' + response.statusText);
        }
        return response.json()
    })
    .then(data => data)
    .catch(error => {
        alert("Error: problem fetching " + url);
        throw error;
    });
}


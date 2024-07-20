
async function getData(reportRunType, reportId, extoleToken) {
    if (!reportId || !extoleToken) {
        return null;
    }
    if (!reportRunType) {
        reportRunType = "report";
    }

    
    var uri;
    if (reportRunType == "report_runner") {
       uri = `/proxy/https://api.extole.io/v6/report-runners/${reportId}/latest/download.json`;
    } else {
       uri = `/proxy/https://api.extole.io/v4/reports/${reportId}/download.json`;
    }

    try {
        const response = await fetch(uri, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${extoleToken}`
            }
        });
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching data:', error);
        throw error;
    }
}

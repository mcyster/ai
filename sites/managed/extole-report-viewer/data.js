
async function getData(reportId, extoleToken) {
    if (!reportId || !extoleToken) {
        return null;
    }

    var uri = `/proxy/https://api.extole.io/v4/reports/${reportId}/download.json`;

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

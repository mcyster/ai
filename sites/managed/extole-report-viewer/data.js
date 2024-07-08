
async function getData(reportId, extoleToken) {
    try {
        const response = await fetch(`https://api.extole.io/v4/tokens/${extoleToken}/download.json?report_id={reportId}`, {
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

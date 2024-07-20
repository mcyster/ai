
async function getData(reportRunnerId) {
    var uri = `/extole/report-runners/${reportRunnerId}/latest/download.json`;

    try {
        const response = await fetch(uri, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
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

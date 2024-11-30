
/**
 * @typedef {Object} Comment
 * @property {string} description
 * @property {string} author
 * @property {string} createdDate
 */

/**
 * @typedef {Object} EngineeringTicket
 * @property {string} key
 * @property {string} project
 * @property {string} summary
 * @property {string} description
 * @property {string} createdDate
 * @property {string} resolvedDate
 * @property {string} priority
 * @property {string} issueType
 * @property {string} status
 * @property {string[]} labels
 * @property {string} reporter
 * @property {string} assignee
 * @property {string} epic
 * @property {string} initiative
 * @property {string} statusChangeDate
 * @property {string} team
 * @property {Comment[]} comments
 */

/**
 * Fetches engineering tickets.
 * @returns {Promise<Ticket[]>}
 */
async function getEngineeringTickets() {
    const url = '/engineering/tickets';

    try {
        const response = await fetch(url, { method: 'GET' });
        if (!response.ok) {
            alert(`Error: unable to fetch ${url}`);
            throw new Error(`Error fetching "${url}": ${response.statusText}`);
        }
        const rawData = await response.json();

        const data = rawData.map(ticket => {
            if (!ticket.createdDate) {
                throw new Error(`Ticket is missing createdDate: ${JSON.stringify(ticket)}`);
            }

            return {
                ...ticket,
                createdDate: new Date(Date.parse(ticket.createdDate)),
                statusChangeDate: new Date(Date.parse(ticket.statusChangeDate)),
                resolvedDate: ticket.resolvedDate ? new Date(Date.parse(ticket.resolvedDate)) : null
           };
        });

        return data;
    } catch (error) {
        alert(`Error: problem fetching ${url}`);
        console.error('Fetch error:', error);
        throw error;
    }
}


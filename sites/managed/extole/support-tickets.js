
/**
 * @typedef {Object} Comment
 * @property {string} description
 * @property {string} author
 * @property {string} createdDate
 */

/**
 * @typedef {Object} Ticket
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
 * @property {string} runbook
 * @property {string|null} runbookUsage
 * @property {string} activity
 * @property {string} activityCategory
 * @property {string} clientShortName
 * @property {string} clientId
 * @property {string} statusChangeDate
 * @property {string} csm
 * @property {string} pod
 * @property {string} clientPriority
 * @property {number} duration
 * @property {string} startDate
 * @property {string|null} requestedStartDate
 * @property {string|null} requestedDueDate
 * @property {Comment[]} comments
 */

/**
 * Fetches support tickets.
 * @returns {Promise<Ticket[]>}
 */
async function getSupportTickets() {
    const url = '/support/tickets';

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

            const createdDate = new Date(Date.parse(ticket.createdDate));

            var startDate = createdDate;
	    if (ticket.requestedStartDate) {
                const requestedStartDate = new Date(Date.parse(ticket.requestedStartDate));
                if (requestedStartDate > startDate) {
		    startDate = requestedStartDate;
	        }
	    }

            return {
                ...ticket,
                createdDate: createdDate,
                startDate: startDate,
                statusChangeDate: new Date(Date.parse(ticket.statusChangeDate)),
                resolvedDate: ticket.resolvedDate ? new Date(Date.parse(ticket.resolvedDate)) : null,
                requestedDueDate: ticket.requestedDueDate ? new Date(Date.parse(ticket.requestedDueDate)) : null,
                requestedStartDate: ticket.requestedStartDate ? new Date(Date.parse(ticket.requestedStartDate)) : null,
                comments: ticket.comments?.map(comment => ({
                    ...comment,
                    createdDate: new Date(Date.parse(comment.createdDate))
                })) || []
           };
        });

        return data;
    } catch (error) {
        alert(`Error: problem fetching ${url}`);
        console.error('Fetch error:', error);
        throw error;
    }
}


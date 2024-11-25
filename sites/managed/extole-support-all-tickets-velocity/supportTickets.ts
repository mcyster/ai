interface SupportTicket {
  key: string;
  project: string;
  description: string;
  comments: string[];
  fields: {
    summary: string;
    clientId: string | null;
    pod: string | null;
    clientShortName: string | null;
    statusChangedDate: string;
    activitiyUsage: string | null;
    project: string;
    reporter: string;
    priority: string;
    labels: string[];
    requestedDueDate: string | null;
    issueType: string;
    timeSeconds: number;
    createdDate: string;
    clientPriority: string | null;
    assignee: string | null;
    category: string;
    startDate: string | null;
    status: string;
    resolvedDate: string | null;
    pairCsm: string | null;
  };
}

async function getSupportTickets(): Promise<SupportTicket[]> {
    const url = '/support/tickets';

    try {
        const response = await fetch(url, { method: 'GET' });
        if (!response.ok) {
            alert(`Error: unable to fetch ${url}`);
            throw new Error(`Error fetching "${url}": ${response.statusText}`);
        }
        const rawData: any[] = await response.json();

        const data: SupportTicket[] = rawData.map(ticket => ({
            ...ticket,
            fields: {
                ...ticket.fields,
                createdDate: new Date(Date.parse(ticket.fields.createdDate)),
                statusChangedDate: new Date(Date.parse(ticket.fields.statusChangedDate)),
                resolvedDate: ticket.fields.resolvedDate ? new Date(Date.parse(ticket.fields.resolvedDate)) : null,
                requestedDueDate: ticket.fields.requestedDueDate ? new Date(Date.parse(ticket.fields.requestDueDate)) : null,
                startDate: ticket.fields.startDate ? new Date(Date.parse(ticket.fields.startDate)) : null
            }
        }));

        return data;
    } catch (error) {
        alert(`Error: problem fetching ${url}`);
        console.error('Fetch error:', error);
        throw error;
    }
}


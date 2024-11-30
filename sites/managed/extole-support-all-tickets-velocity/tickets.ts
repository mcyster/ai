

console.log("tikcets");

interface Comment {
  description: string;
  author: string;
  createdDate: string;
}

interface Ticket {
  key: string;
  project: string;
  summary: string;
  description: string;
  createdDate: string;
  resolvedDate: string;
  priority: string;
  issueType: string;
  status: string;
  labels: string[];
  reporter: string;
  assignee: string;
  runbook: string;
  runbookUsage: string | null;
  activity: string;
  detailedActivity: string;
  clientShortName: string;
  clientId: string;
  statusChangeDate: string;
  csm: string;
  pod: string;
  clientPriority: string;
  duration: number;
  startDate: string;
  requestedStartDate: string | null;
  requestedDueDate: string | null;
  comments: Comment[];
}

async function getSupportTickets(): Promise<Ticket[]> {
    const url = '/support/tickets';

    try {
        const response = await fetch(url, { method: 'GET' });
        if (!response.ok) {
            alert(`Error: unable to fetch ${url}`);
            throw new Error(`Error fetching "${url}": ${response.statusText}`);
        }
        const rawData: any[] = await response.json();

        const data: Ticket[] = rawData.map(ticket => ({
            ...ticket,
            createdDate: new Date(Date.parse(ticket.createdDate)).toISOString(),
            startDate: ticket.startDate ? new Date(Date.parse(ticket.startDate)).toISOString() : null,
            statusChangeDate: new Date(Date.parse(ticket.statusChangeDate)).toISOString(),
            resolvedDate: ticket.resolvedDate ? new Date(Date.parse(ticket.resolvedDate)).toISOString() : null,
            requestedDueDate: ticket.requestedDueDate ? new Date(Date.parse(ticket.requestedDueDate)).toISOString() : null,
            requestedStartDate: ticket.requestedStartDate ? new Date(Date.parse(ticket.requestedStartDate)).toISOString() : null
        }));

        return data;
    } catch (error) {
        alert(`Error: problem fetching ${url}`);
        console.error('Fetch error:', error);
        throw error;
    }
}


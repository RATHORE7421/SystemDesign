package src.HLDProblems;
// createIssue(transactionId, issueType, subject, description, email)
// addAgent(agentEmail, agentName ,List<issueType>)
// assignIssue(issueId) // -> Issue can be assigned to the agents based on different strategies. For now, assign to any one of the free agents.
// getIssues(filter) // -> issues against the provided filter
// updateIssue(issueId, status, resolution)
// resolveIssue(issueId, resolution)
// viewAgentsWorkHistory() // -> a list of issue which agents worked on

import java.util.*;

enum IssueType {
    PAYMENT_RELATED, 
    MUTUAL_FUND_RELATED, 
    GOLD_RELATED,
    INSURANCE_RELATED
}

enum IssueStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED
}

class Issue {
    String issueId;
    String transactionId;
    IssueType type;
    String subject;
    String description;
    String email;
    IssueStatus status;
    String resolution;
    Agent agent;

    public Issue(String issueId, String transactionId, IssueType type,
                 String subject, String description, String email) {
        this.issueId = issueId;
        this.transactionId = transactionId;
        this.type = type;
        this.subject = subject;
        this.description = description;
        this.email = email;
        this.status = IssueStatus.OPEN;
    }

    @Override
    public String toString() {
        return issueId + " {\"" + transactionId + "\", \"" + type + "\", \"" + subject + "\", \"" + description + "\", \"" + email + "\", \"" + status + "\"} ";
    }
}

class Agent {
    String agentId;
    String email;
    String name;
    List<IssueType> expertise;
    Queue<Issue> waitlist; //To insert issues in the order they are created and have not yet assigned
    List<Issue> workHistory;
    boolean isAvailable;

    public Agent(String agentId, String email, String name, List<IssueType> expertise) {
        this.agentId = agentId;
        this.email = email;
        this.name = name;
        this.expertise = expertise;
        this.waitlist = new LinkedList<>();
        this.workHistory = new ArrayList<>();
        this.isAvailable = true;
    }
}

public class CustomerIssueResolutionSystem {
    Map<String, Issue> issues = new HashMap<>();
    Map<String, Agent> agents = new HashMap<>();
    Map<String, String> issueToAgent = new HashMap<>();
    int issueCounter = 1;
    int agentCounter = 1;

    public void createIssue(String transactionId, String issueTypeStr, String subject, String desc, String email) {
        String issueId = "I" + issueCounter++;
        IssueType issueType = IssueType.valueOf(issueTypeStr.toUpperCase().replace(" ", "_"));
        Issue issue = new Issue(issueId, transactionId, issueType, subject, desc, email);
        issues.put(issueId, issue);
        System.out.println("Issue " + issueId + " created against transaction " + transactionId);
    }

    public void addAgent(String agentEmail, String agentName, List<String> issueTypes) {
        String agentId = "A" + agentCounter++;
        List<IssueType> types = new ArrayList<>();
        for(String t: issueTypes) {
            types.add(IssueType.valueOf(t.toUpperCase().replace(" ", "_")));
        }
        Agent agent = new Agent(agentId, agentEmail, agentName, types);
        agents.put(agentId, agent);
        System.out.println("Agent " + agentId + " created");
    }

    public void assignIssue(String issueId) {
        Issue issue = issues.get(issueId); // this is the reason why I created map of issues;
        for(Agent agent : agents.values()) {
            if(agent.expertise.contains(issue.type)) {
                if(agent.isAvailable) {
                    agent.workHistory.add(issue);
                    agent.isAvailable = false;
                    issueToAgent.put(issue.issueId, agent.agentId);
                    System.out.println("Issue " + issueId + " assigned to agent " + agent.agentId);
                    return;
                } else {
                    // Handeled edge case1, when no agent willbe available
                    agent.waitlist.add(issue);
                    issueToAgent.put(issue.issueId, agent.agentId);
                    System.out.println("Issue " + issueId + " added to waitlist of Agent " + agent.agentId);
                    return;
                }
            }
        }
        // Handeled edge case2, when issueType doesn't match any agent
        System.out.println("No available agents with  matching expertise for issue " + issueId);
    }

    public List<Issue> getIssue(Map<String, String> filter) {
        List<Issue> result = new ArrayList<>();
        for(Issue issue : issues.values()){
            if((filter.containsKey("email") && issue.email.equals(filter.get("email"))) ||
                (filter.containsKey("type") && issue.type.name().equalsIgnoreCase(filter.get("type").replace(" ", "_")))) {
                    result.add(issue);
                }
        }
        for(Issue i : result) System.out.println(i);
        return result;
    }

    public void updateIssue(String issueId, String statusStr, String resolution) {
        Issue issue = issues.get(issueId);
        issue.status = IssueStatus.valueOf(statusStr.toUpperCase().replace(" ", "_"));
        issue.resolution = resolution;
        System.out.println(issueId + " status updated to " + issue.status);
    }

    public void resolveIssue(String issueId, String resolution) {
        Issue issue = issues.get(issueId);
        issue.status = IssueStatus.RESOLVED;
        issue.resolution = resolution;
        System.out.println(issueId + " issue marked resolved");

        // Since above agent gets free so need to assign issues that are in waitlist
        String agentId = issueToAgent.get(issue.issueId);
        Agent agent = agents.get(agentId);
        agent.isAvailable = true;
        if(!agent.waitlist.isEmpty()) {
            Issue nextIssue = agent.waitlist.poll();
            agent.workHistory.add(nextIssue); // Add to work history when taking from waitlist
            agent.isAvailable = false;
        }
    }

    public void viewAgentsWorkHistory() {
        for(Map.Entry<String, Agent> entry : agents.entrySet()) {
            System.out.print(entry.getKey() + " -> {");
            List<String> ids = new ArrayList<>();
            for(Issue i : entry.getValue().workHistory) {
                ids.add(i.issueId);
            }
            System.out.println(String.join(", ", ids) + "}");
        }
    }
    public static void main(String args[]) {
        CustomerIssueResolutionSystem system = new CustomerIssueResolutionSystem();

        system.createIssue("T1", "Payment Related", "Payment Failed", "My payment failed but money is debited", "testUser1@test.com");
        system.createIssue("T2", "Mutual Fund Related", "Purchase Failed", "Unable to purchase Mutual Fund", "testUser2@test.com");
        system.createIssue("T3", "Payment Related", "Payment Failed", "My payment failed but money is debited", "testUser2@test.com");

        system.addAgent("agent1@test.com", "Agent 1", Arrays.asList("Payment Related", "Gold Related"));
        system.addAgent("agent2@test.com", "Agent 2", Arrays.asList("Mutual Fund Related"));

        system.assignIssue("I1");
        system.assignIssue("I2");
        system.assignIssue("I3");

        Map<String, String> f1 = new HashMap<>();
        f1.put("email", "testUser2@test.com");
        system.getIssue(f1);

        Map<String, String> f2 = new HashMap<>();
        f2.put("type", "Payment Related");
        system.getIssue(f2);

        system.updateIssue("I3", "In Progress", "Waiting for payment confirmation");
        system.resolveIssue("I3", "PaymentFailed debited amount will get reversed");
        system.viewAgentsWorkHistory();
    }
}
--delink
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.event.api.EventListener
import com.atlassian.jira.event.type.EventType
import com.atlassian.jira.component.ComponentAccessor

@EventListener
def issueEventListener(IssueEvent issueEvent) {
    def issue = issueEvent.issue
    def issueManager = ComponentAccessor.issueManager
    def transitionManager = ComponentAccessor.getWorkflowManager().getWorkflowTransition(issue)
    def childIssues = issue.getSubTaskObjects()

    if (issueEvent.eventTypeId == EventType.ISSUE_LINK_DELETED_ID && issue.isSubTask()) {
        def parentIssue = issue.getParentObject()
        def allChildren = parentIssue.getSubTaskObjects()

        if (allChildren.isEmpty()) {
            def transition = transitionManager.getTransition("101") // replace with the actual transition ID
            transitionManager.transition(parentIssue, transition)
        }
    }
}


--when link
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.event.api.EventListener
import com.atlassian.jira.event.type.EventType
import com.atlassian.jira.component.ComponentAccessor

@EventListener
def issueEventListener(IssueEvent issueEvent) {
    def issue = issueEvent.issue
    def issueManager = ComponentAccessor.issueManager
    def transitionManager = ComponentAccessor.getWorkflowManager().getWorkflowTransition(issue)
    def childIssues = issue.getSubTaskObjects()

    if (issueEvent.eventTypeId == EventType.ISSUE_CREATED_ID && issue.isSubTask()) {
        def parentIssue = issue.getParentObject()
        def transition = transitionManager.getTransition("101") // replace with the actual transition ID
        transitionManager.transition(parentIssue, transition)
    }
}


--if all link issue closed --
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil

def issueManager = ComponentAccessor.issueManager
def issueLinkManager = ComponentAccessor.getIssueLinkManager()
def workflowTransitionUtil = ComponentAccessor.getWorkflowTransitionUtil()

def issue = issueManager.getIssueObject(issue.key) // Replace with the current issue

if (issue.issuetype.name == "Epic") {
    def linkedIssues = issueLinkManager.getOutwardLinks(issue.id)

    def allClosed = true
    linkedIssues.each {
        IssueLink link ->
            def linkedIssue = issueManager.getIssueObject(link.destinationId)
            if (linkedIssue.isSubTask()) {
                allClosed = allClosed && linkedIssue.getStatusObject().name == "Closed"
            } else {
                allClosed = allClosed && isIssueTreeClosed(linkedIssue)
            }
    }

    if (allClosed) {
        def transition = workflowTransitionUtil.getTransition(issue, "101") // Replace with the actual transition ID
        workflowTransitionUtil.transition(issue, transition)
    }
}

def isIssueTreeClosed(Issue issue) {
    def linkedIssues = issueLinkManager.getOutwardLinks(issue.id)

    def allClosed = true
    linkedIssues.each {
        IssueLink link ->
            def linkedIssue = issueManager.getIssueObject(link.destinationId)
            allClosed = allClosed && linkedIssue.getStatusObject().name == "Closed"
    }

    return allClosed
}


---

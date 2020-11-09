package de.mfdz.osmrf.validation;

import java.util.HashMap;
import java.util.Map;

public class ComparisonResult {
    public static final ComparisonResult NO_DIFF = new ComparisonResult(true, null);

    private Boolean pathsAreEqual;
    private String diffReason;
    private Map<String, String> issues = new HashMap<String, String>();

    public Boolean getPathsAreEqual() {
        return pathsAreEqual;
    }

    public String getDiffReason() {
        return diffReason;
    }

    public ComparisonResult() {

    }

    public ComparisonResult(boolean pathsAreEqual, String diffReason) {
        this.pathsAreEqual = pathsAreEqual;
        this.diffReason = diffReason;
    }

    public Map<String, String> getIssues(){
        return issues;
    }

    public void addIssue(String header, String details) {
        issues.put(header, details);
    }

    public void addIssuesFrom(ComparisonResult anotherResult) {
        for (Map.Entry entry: anotherResult.getIssues().entrySet()) {
            this.addIssue((String)entry.getKey(), (String) entry.getKey());
        }
    }
}

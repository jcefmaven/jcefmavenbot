package me.friwi.jcefmavenbot.github;

public enum GitHubAPIIssueLockReason {
    OFF_TOPIC("off-topic"),
    TOO_HEATED("too heated"),
    RESOLVED("resolved"),
    SPAM("spam");

    private String value;

    GitHubAPIIssueLockReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GitHubAPIIssueLockReason fromString(String str){
        if(str==null)return null;
        for(GitHubAPIIssueLockReason r : values()){
            if(r.getValue().equals(str))return r;
        }
        return null;
    }
}

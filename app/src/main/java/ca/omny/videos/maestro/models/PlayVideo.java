package ca.omny.videos.maestro.models;

public class PlayVideo {
    private String title;
    private String[] sources;
    private String[] subtitleSources;
    private int startTime;

    public PlayVideo(String title, String[] sources, String[] subtitleSources, int startTime) {
        this.title = title;
        this.sources = sources;
        this.subtitleSources = subtitleSources;
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public String[] getSources() {
        return sources;
    }

    public String[] getSubtitleSources() {
        return subtitleSources;
    }

    public int getStartTime() {
        return startTime;
    }
}

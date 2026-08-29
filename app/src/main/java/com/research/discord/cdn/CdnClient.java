package com.research.discord.cdn;

public class CdnClient {
    private static final String BASE_URL = "https://cdn.discordapp.com";

    public static String getDefaultAvatarUrl(String discriminator) {
        int index = 0;
        try {
            index = Integer.parseInt(discriminator) % 5;
        } catch (NumberFormatException ignored) {}
        return BASE_URL + "/embed/avatars/" + index + ".png";
    }

    public static String getUserAvatarUrl(String userId, String avatarHash) {
        return BASE_URL + "/avatars/" + userId + "/" + avatarHash + ".webp?size=128";
    }

    public static String getGuildIconUrl(String guildId, String iconHash) {
        return BASE_URL + "/icons/" + guildId + "/" + iconHash + ".webp?size=128";
    }

    public static String getGuildBannerUrl(String guildId, String bannerHash) {
        return BASE_URL + "/banners/" + guildId + "/" + bannerHash + ".webp?size=1024";
    }

    public static String getEmojiUrl(String emojiId, boolean animated) {
        String ext = animated ? "gif" : "webp";
        return BASE_URL + "/emojis/" + emojiId + "." + ext + "?size=64";
    }
}

package plugin.artimc.engine.title;

import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class Time implements Title.Times {
    final long _fadeIn;
    final long _stay;
    final long _fadeOut;

    public Time(long fadeIn, long stay, long fadeOut) {
        _fadeIn = fadeIn;
        _stay = stay;
        _fadeOut = fadeOut;
    }


    @Override
    public @NotNull Duration fadeIn() {
        return Duration.ofMillis(250);
    }

    @Override
    public @NotNull Duration stay() {
        return Duration.ofMillis(500);
    }

    @Override
    public @NotNull Duration fadeOut() {
        return Duration.ofMillis(250);
    }
}

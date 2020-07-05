package com.sili.service.utils;

import io.smallrye.mutiny.tuples.Tuple3;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class AuthUtils {

    private final Random r = new Random();

    public Tuple3<UUID, Long, Long> getAuthStateInitValues(String username, Long userId) {
        return Tuple3.of(getToken(username), userId, (long) getReps());
    }

    private UUID getToken(String username) {
        // TODO generate from username
        return UUID.randomUUID();
    }

    private int getReps() {
        // TODO define maximal number of tries in app properties
        return new Random().nextInt(5) + 1;
    }

    public List<Long> generateAVector(int length) {
        return IntStream.generate(() -> r.nextInt(2))
            .limit(length)
            .mapToLong(Long::valueOf)
            .boxed()
            .collect(Collectors.toList());
    }
}

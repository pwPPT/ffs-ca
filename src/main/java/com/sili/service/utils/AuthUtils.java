package com.sili.service.utils;

import com.sili.model.SessionTO;
import com.sili.model.SuccessTO;
import io.smallrye.mutiny.tuples.Tuple3;
import java.util.List;
import java.util.ArrayList;
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

    public Boolean checkYValue(List<Integer> yVector, Long providedY) {
    // public Boolean checkYValue(List<Integer> publicKey, Long providedX, Long providedY, List<Integer> generatedA, Long N) {
    //     List<Long> values = new ArrayList<>();
    //     for(int i = 0; i < publicKey.size(); i++) {
    //         if(generatedA.get(i) == 1) {
    //             values.add(Long.valueOf(publicKey.get(i)));
    //         } else {
    //             values.add(Long.valueOf(1));
    //         }
    //     }

    //     Long y1 = values.stream().reduce(providedX, (subtotal, element) -> (subtotal * element) % N);
    //     Long y2 = (providedY * providedY) % N;
    //     return y1 == y2;
    return true;
    }

    public SessionTO isAuthorized(SuccessTO status) {
        boolean repeat = status.getReps() > status.getSuccTries();
        boolean authorized = status.getReps() == status.getSuccTries();
        String sessionID = authorized ? UUID.randomUUID().toString() : null;

        return new SessionTO(repeat, authorized, sessionID);
    }
}

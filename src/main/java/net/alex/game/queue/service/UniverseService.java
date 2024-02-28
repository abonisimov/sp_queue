package net.alex.game.queue.service;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.Colony;
import net.alex.game.model.Universe;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UniverseService {

    public List<Colony> getColoniesList(String universeId) {
        return Collections.emptyList(); //todo: implement
    }

    public void createUniverse(Universe universe) {
        //todo: implement
    }

    public void deleteUniverse(String universeId) {
        //todo: implement
    }

    public void startUniverse(String universeId) {
        //todo: implement
    }

    public void stopUniverse(String universeId) {
        //todo: implement
    }

    public Universe getUniverse(String universeId) {
        return null; //todo: implement
    }

    public List<Universe> getUniversesList() {
        return Collections.emptyList(); //todo: implement
    }
}

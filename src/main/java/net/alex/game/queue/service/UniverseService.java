package net.alex.game.queue.service;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.ColonyDescription;
import net.alex.game.model.Universe;
import net.alex.game.model.UniverseDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UniverseService {

    public Page<ColonyDescription> getColoniesList(String universeId, Pageable pageable) {
        return Page.empty(); //todo: implement
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

    public Page<UniverseDescription> getUniversesList(Pageable pageable) {
        return Page.empty(); //todo: implement
    }
}

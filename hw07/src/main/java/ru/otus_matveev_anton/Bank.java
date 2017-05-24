package ru.otus_matveev_anton;

import ru.otus_matveev_anton.atm.ATM;

import java.util.ArrayList;
import java.util.List;

public class Bank {

    private List<ATM> atms = new ArrayList<>();

    int getBalance(){
        return atms.stream().mapToInt(ATM::getBalance).reduce(Integer::sum).orElse(0);
    }

    void downloadATMs(){
        atms.forEach(ATM::download);
    }

    void addATM(ATM atm){
        atms.add(atm);
    }

    boolean removeATM(ATM atm){
        return atms.remove(atm);
    }
}

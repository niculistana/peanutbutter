package com.somamission.peanutbutter.impl;

import com.somamission.peanutbutter.domain.ReservedWord;
import com.somamission.peanutbutter.intf.IReservedWordService;
import com.somamission.peanutbutter.repository.IReservedWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservedWordService implements IReservedWordService {

  @Autowired IReservedWordRepository reservedWordRepository;

  @Override
  public List<ReservedWord> getAllReservedWords() {
    List<ReservedWord> reservedWords = new ArrayList<>();

    for (ReservedWord reservedWord : reservedWordRepository.findAll()) {
      reservedWords.add(reservedWord);
    }

    return reservedWords;
  }
}

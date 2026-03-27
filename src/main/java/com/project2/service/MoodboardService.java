package com.project2.service;

import com.project2.entity.Moodboard;
import com.project2.repository.MoodboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class MoodboardService {

    @Autowired
    private MoodboardRepository moodboardRepository;

    public List<Moodboard> getAllMoodboards() {
        return moodboardRepository.findAll();
    }

    public Optional<Moodboard> getMoodboardById(Long id) {
        return moodboardRepository.findById(id);
    }

    public Optional<Moodboard> getMoodboardBySlug(String slug) {
        return moodboardRepository.findFirstBySlug(slug);
    }

    public Moodboard saveMoodboard(Moodboard moodboard) {
        return moodboardRepository.save(moodboard);
    }

    public void deleteMoodboard(Long id) {
        moodboardRepository.deleteById(id);
    }
}

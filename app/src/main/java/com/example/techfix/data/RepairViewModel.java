package com.example.techfix.data;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class RepairViewModel extends AndroidViewModel {
    public RepairViewModel(android.app.Application application) { super(application); }

    public LiveData<List<RepairJob>> jobsForUser(int userId) {
        return TechFixDatabase.get(getApplication()).repairDao().jobsForUser(userId);
    }
}

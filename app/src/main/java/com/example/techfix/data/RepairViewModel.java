package com.example.techfix.data;
import androidx.lifecycle.*;
public class RepairViewModel extends AndroidViewModel { public final LiveData<java.util.List<RepairJob>> jobs; public RepairViewModel(android.app.Application a){super(a);jobs=TechFixDatabase.get(a).repairDao().jobs();} }

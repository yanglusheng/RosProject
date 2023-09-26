package com.example.rosproject.Core;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface Savable {
    void load(@NonNull Bundle bundle);

    void save(@NonNull Bundle bundle);
}

package com.dhis2.usescases.main;


import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;

import io.reactivex.functions.Consumer;

public final class MainContracts {

    interface View extends AbstractActivityContracts.View {

        @NonNull
        @UiThread
        Consumer<String> renderUsername();

        @NonNull
        @UiThread
        Consumer<String> renderUserInfo();

        @NonNull
        @UiThread
        Consumer<String> renderUserInitials();

    }

    public interface Presenter {
        void init(View view);

        void onDetach();

        public void logOut();
    }

    interface Interactor {
    }

    interface Router {

    }

}
package com.hvzhub.app;

interface OnRefreshIsHumanListener {
    void OnRefreshIsHuman(OnIsHumanRefreshedListener listener);

    interface OnIsHumanRefreshedListener {
        void OnIsHumanRefreshed();
    }
}




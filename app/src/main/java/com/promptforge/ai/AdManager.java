package com.promptforge.ai;

import android.app.Activity;
import android.content.Context;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public final class AdManager {
    // Google test IDs. Replace with your production AdMob IDs before publishing.
    private static final String TEST_INTERSTITIAL="ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_REWARDED="ca-app-pub-3940256099942544/5224354917";

    private final Activity activity;
    private ConsentInformation consent;
    private InterstitialAd interstitial;
    private RewardedAd rewarded;
    private int actions=0;
    private boolean ready=false;

    public AdManager(Activity a){activity=a;}

    public void start(){
        consent=UserMessagingPlatform.getConsentInformation(activity);
        ConsentRequestParameters params=new ConsentRequestParameters.Builder().build();
        consent.requestConsentInfoUpdate(activity,params,
            ()->UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,error->initAds()),
            error->initAds());
    }

    private void initAds(){
        if(consent!=null && !consent.canRequestAds()) return;
        MobileAds.initialize(activity,status->{ready=true;loadInterstitial();loadRewarded();});
    }

    private void loadInterstitial(){
        if(!ready || interstitial!=null)return;
        InterstitialAd.load(activity,TEST_INTERSTITIAL,new AdRequest.Builder().build(),
            new InterstitialAdLoadCallback(){
                @Override public void onAdLoaded(InterstitialAd ad){interstitial=ad;}
                @Override public void onAdFailedToLoad(LoadAdError e){interstitial=null;}
            });
    }

    private void loadRewarded(){
        if(!ready || rewarded!=null)return;
        RewardedAd.load(activity,TEST_REWARDED,new AdRequest.Builder().build(),
            new RewardedAdLoadCallback(){
                @Override public void onAdLoaded(RewardedAd ad){rewarded=ad;}
                @Override public void onAdFailedToLoad(LoadAdError e){rewarded=null;}
            });
    }

    // Compliant frequency: at most one interstitial after every two meaningful
    // generation actions, and only at the result transition.
    public void onGenerationCompleted(boolean premium){
        if(premium)return;
        actions++;
        if(actions<2)return;
        actions=0;
        if(interstitial==null){loadInterstitial();return;}
        InterstitialAd ad=interstitial;
        interstitial=null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override public void onAdDismissedFullScreenContent(){loadInterstitial();}
            @Override public void onAdFailedToShowFullScreenContent(AdError e){loadInterstitial();}
        });
        ad.show(activity);
    }

    public boolean showRewardedForBonus(Runnable reward){
        if(!ready || rewarded==null)return false;
        RewardedAd ad=rewarded;
        rewarded=null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override public void onAdDismissedFullScreenContent(){loadRewarded();}
            @Override public void onAdFailedToShowFullScreenContent(AdError e){loadRewarded();}
        });
        ad.show(activity,new OnUserEarnedRewardListener(){
            @Override public void onUserEarnedReward(RewardItem item){reward.run();}
        });
        return true;
    }
}

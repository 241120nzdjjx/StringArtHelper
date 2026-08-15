/*
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 牛杂の经济学
 */
package com.niuza.stringartreader2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.pdf.PdfDocument;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.InputType;
import android.util.AtomicFile;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.auth.AuthApi;
import com.xiaomi.xms.wearable.auth.Permission;
import com.xiaomi.xms.wearable.message.MessageApi;
import com.xiaomi.xms.wearable.message.OnMessageReceivedListener;
import com.xiaomi.xms.wearable.node.Node;
import com.xiaomi.xms.wearable.node.NodeApi;
import com.xiaomi.xms.wearable.tasks.OnFailureListener;
import com.xiaomi.xms.wearable.tasks.OnSuccessListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

public final class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_OPEN_TXT = 1001;
    private static final int REQUEST_OPEN_IMAGE = 1002;
    private static final int REQUEST_CREATE_TEMPLATE_PDF = 1003;
    private static final int REQUEST_CREATE_SEQUENCE_TXT = 1004;
    private static final int REQUEST_CREATE_SAVE = 1005;
    private static final int REQUEST_OPEN_SAVE = 1006;
    private static final int MAX_FILE_BYTES = 8 * 1024 * 1024;
    private static final int MAX_IMPORTED_SAVE_BYTES = 16 * 1024 * 1024;
    private static final int MAX_SEQUENCE_LENGTH = 100_000;
    private static final String SAVE_MAGIC_LEGACY = "SAR2";
    private static final String SAVE_MAGIC_GEOMETRY = "SAR3";
    private static final String SAVE_MAGIC = "SAR4";
    private static final int SAVE_THUMBNAIL_SIZE = 192;
    private static final int MAX_SAVE_THUMBNAIL_BYTES = 256 * 1024;
    private static final float MIN_THREAD_MM = .01f;
    private static final float MAX_THREAD_MM = 1f;
    private static final float DEFAULT_IMPORTED_THREAD_MM = .20f;
    private static final int MIN_CIRCLE_MM = 80;
    private static final int MAX_CIRCLE_MM = 1200;
    private static final int DEFAULT_IMPORTED_CIRCLE_MM = 260;
    private static final int MIN_SPEECH_DELAY_MS = 500;
    private static final int MAX_SPEECH_DELAY_MS = 10_000;
    private static final int QUICK_DELAY_STEP_MS = 500;

    private static final int BG = Color.rgb(16, 16, 22);
    private static final int PANEL = Color.rgb(29, 29, 39);
    private static final int PANEL_2 = Color.rgb(38, 38, 50);
    private static final int FG = Color.rgb(245, 245, 250);
    private static final int MUTED = Color.rgb(157, 157, 174);
    private static final int ACCENT = Color.rgb(151, 105, 255);
    private static final int ACCENT_DARK = Color.rgb(112, 74, 207);
    private static final int PREVIEW_BG = Color.rgb(247, 246, 250);
    private static final String BILIBILI_URL = "https://b23.tv/K3Cp0ZZ";
    private static final String CONTACT_EMAIL = "241120nzdjjx@gmail.com";
    private static final String X_URL = "https://x.com/nzdjjx241120";
    private static final String TELEGRAM_URL = "https://t.me/nzdjjx";
    private static final String GITHUB_URL = "https://github.com/241120nzdjjx/StringArtHelper";
    private static final String WECHAT_MINIPROGRAM_LINK = "#小程序://绕线画助手/3BsgpZwfWylWK9d";

    private static final String PREFS = "string_art_reader_v2";
    private static final String KEY_SEQUENCE = "sequence";
    private static final String KEY_INDEX = "index";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_DELAY = "delay_ms";
    private static final String KEY_RATE = "speech_rate";
    private static final String KEY_TTS_VOLUME_PERCENT = "tts_volume_percent";
    private static final String KEY_REPEAT = "repeat_twice";
    private static final String KEY_VOLUME_KEYS = "volume_keys";
    private static final String KEY_MEDIA_DUCKING = "media_ducking";
    private static final String KEY_PREVIEW_VISIBLE = "preview_visible";
    private static final String KEY_PREVIEW_MIN = "preview_minimized";
    private static final String KEY_PREVIEW_CUSTOM_LINE_MM = "preview_custom_line_mm";
    private static final String KEY_PREVIEW_USE_ACTUAL_RATIO = "preview_use_actual_ratio";
    private static final String KEY_PROJECT_NAILS = "project_nails";
    private static final String KEY_PROJECT_CIRCLE_MM = "project_circle_mm";
    private static final String KEY_PROJECT_LINE_MM = "project_line_mm";
    private static final String KEY_GENERATOR_AUTO_STOP = "generator_auto_stop";
    private static final String KEY_GENERATOR_VISUAL_SPEED = "generator_visual_speed";
    private static final String KEY_PREVIEW_ANIMATION_SPEED = "preview_animation_speed";
    private static final String KEY_ACTIVE_PROJECT_FILE = "active_project_file";
    private static final String KEY_ACTIVE_PROJECT_NAME = "active_project_name";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_WEARABLE_CONTROL = "wearable_control_enabled";
    private static final String KEY_PHONE_VIBRATION = "phone_vibration_enabled";
    private static final String KEY_VOLUME_UP_ACTION = "volume_up_action";
    private static final String KEY_VOLUME_DOWN_ACTION = "volume_down_action";
    private static final String KEY_WATCH_VIBRATION = "watch_vibration_enabled";
    private static final String KEY_WATCH_KEEP_ON = "watch_keep_screen_on";
    private static final String KEY_WATCH_OUTWARD_ACTION = "watch_outward_action";
    private static final String KEY_WATCH_INWARD_ACTION = "watch_inward_action";
    private static final int WEARABLE_CHUNK_BYTES = 768;

    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern NAILS_METADATA_PATTERN = Pattern.compile(
            "(?i)(?:(?:钉(?:子)?数|nails?|pins?)\\s*[:：=_-]?\\s*(\\d{1,6})"
                    + "|(\\d{1,6})\\s*(?:钉|nails?|pins?))");
    private static final Pattern THREAD_METADATA_PATTERN = Pattern.compile(
            "(?i)(?:线径|线直径|thread(?:\\s+diameter)?|line\\s+diameter)"
                    + "\\s*[:：=_-]?\\s*(\\d+(?:\\.\\d+)?)\\s*(?:mm|毫米)?");
    private static final Pattern CIRCLE_METADATA_PATTERN = Pattern.compile(
            "(?i)(?:钉位圆(?:直径)?|圆径|nail\\s+circle(?:\\s+diameter)?"
                    + "|circle\\s+diameter)"
                    + "\\s*[:：=_-]?\\s*(\\d+(?:\\.\\d+)?)\\s*(?:mm|毫米)?");
    private static final Pattern TXT_EXPORT_SUFFIX_PATTERN = Pattern.compile(
            "(?i)[_\\s-]+\\d{1,6}(?:(?:钉)[_\\s-]*(?:线径|线直径)"
                    + "|[_\\s-]*(?:nails?|pins?)[_\\s-]*(?:thread|line))"
                    + "[_\\s-]*\\d+(?:\\.\\d+)?\\s*mm"
                    + "(?:[_\\s-]*(?:圆径|钉位圆|circle)[_\\s-]*\\d+(?:\\.\\d+)?"
                    + "\\s*mm)?$");
    private static final Pattern SEQUENCE_NAME_SUFFIX_PATTERN = Pattern.compile(
            "(?i)[_\\s-]+(?:绕线序列|string[_\\s-]+sequence)$");
    private static final Pattern GENERATED_LABEL_PREFIX_PATTERN = Pattern.compile(
            "(?i)^(?:生成|generated)\\s*·\\s*");
    private static final Pattern GENERATED_LABEL_SUFFIX_PATTERN = Pattern.compile(
            "(?i)\\s*·\\s*\\d{1,6}\\s*(?:钉|nails?)"
                    + "\\s*·\\s*\\d+(?:\\.\\d+)?\\s*mm$");
    private static final Pattern STEP_PREFIX_PATTERN = Pattern.compile(
            "^\\s*(?:(?:step|line|row|route|index|第|步骤|线路)\\s*)?\\d+\\s*[:：.)、]\\s*(.+)$",
            Pattern.CASE_INSENSITIVE);

    private final ArrayList<Integer> sequence = new ArrayList<Integer>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler previewAnimationHandler = new Handler(Looper.getMainLooper());
    private final Handler jumpUndoHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService generatorExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean generatorCancelled = new AtomicBoolean(false);

    private SharedPreferences prefs;
    private TextToSpeech tts;
    private AudioManager audioManager;
    private MediaSession mediaSession;
    private NodeApi wearableNodeApi;
    private MessageApi wearableMessageApi;
    private AuthApi wearableAuthApi;
    private String wearableNodeId;
    private boolean wearableControlEnabled;
    private boolean wearableListenerRegistered;
    private String wearableStatus = "手环控制未开启";
    private String lastWearableState = "";
    private TextView wearableStatusView;
    private boolean wearablePageVisible;
    private final OnMessageReceivedListener wearableMessageListener =
            new OnMessageReceivedListener() {
                @Override public void onMessageReceived(final String nodeId, final byte[] data) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            handleWearableMessage(nodeId,
                                    new String(data, StandardCharsets.UTF_8));
                        }
                    });
                }
            };
    private boolean ttsReady = false;
    private boolean isPlaying = false;
    private volatile boolean activityDestroyed = false;
    private int currentIndex = 0;
    private int playGeneration = 0;
    private int delayMs = 2500;
    private float speechRate = 0.82f;
    private int ttsVolumePercent = 100;
    private boolean repeatTwice = false;
    private boolean volumeKeysEnabled = false;
    private boolean phoneVibrationEnabled = false;
    private String volumeUpAction = "previous";
    private String volumeDownAction = "next";
    private boolean watchVibrationEnabled = true;
    private boolean watchKeepScreenOn = true;
    private String watchOutwardAction = "next";
    private String watchInwardAction = "previous";
    private long wearableConfigRevision = 1L;
    private ArrayList<JSONObject> wearableTransferQueue = new ArrayList<JSONObject>();
    private int wearableTransferPosition = -1;
    private boolean wearableTransferWaiting;
    private boolean mediaDuckingEnabled = true;
    private long lastForwardStepAtMs = 0L;
    private double observedStepDurationMs = 0d;
    private int observedStepSamples = 0;
    private AudioFocusRequest speechFocusRequest;
    private boolean speechFocusHeld;
    private volatile String activeTtsUtteranceId;
    private String importedFileName = "未导入序列";
    private File activeAutoProjectFile;
    private String activeAutoProjectName;
    private Bitmap pendingGeneratorBitmap;
    private byte[] currentProjectThumbnail;
    private byte[] pendingGeneratedThumbnail;
    private String pendingGeneratorName = "图片";
    private Dialog generatorProgressDialog;
    private Dialog generatorConfigDialog;
    private AlertDialog generatedPreviewDialog;
    private Runnable generatorAnimationRunnable;
    private int pendingTemplateNails;
    private int pendingTemplateDiameterMm;
    private int generatorNails = 220;
    private int generatorSteps = 4000;
    private int generatorCircleMm = 260;
    private float generatorLineMm = 0.10f;
    private boolean generatorAutoStop = true;
    private int generatorVisualSpeed = 5;
    private int previewAnimationSpeed = 5;
    private float generatorCropX = .5f, generatorCropY = .5f, generatorCropZoom = 1f;
    private ArrayList<Integer> generatedCandidate;
    private ArrayList<Integer> pendingExportSequence;
    private int pendingExportNails;
    private float pendingExportLineMm;
    private int pendingExportCircleMm;
    private File pendingSaveExportFile;

    private boolean landscape;
    private boolean previewVisible = true;
    private boolean previewMinimized = false;
    private float previewCustomLineMm = 0.10f;
    private boolean previewUseActualRatio = true;
    private int pendingStepAnimation = 0;
    private boolean previewAnimationRunning = false;
    private boolean previewAnimationResultHeld = false;
    private boolean previewAnimationWasPlaying = false;
    private boolean previewAnimationHolding = false;
    private int previewAnimationIndex = -1;
    private Runnable previewAnimationRunnable;
    private final ArrayList<View> previewAnimationLockedViews = new ArrayList<View>();
    private final ArrayList<Boolean> previewAnimationLockedEnabled = new ArrayList<Boolean>();
    private final ArrayList<Float> previewAnimationLockedAlpha = new ArrayList<Float>();

    private LinearLayout root;
    private LinearLayout mainPane;
    private LinearLayout quickSettingsRow;
    private LinearLayout previewContainer;
    private LinearLayout previewBody;
    private StringArtPreview previewView;
    private StringArtPreview fullscreenPreview;
    private Dialog fullscreenDialog;
    private SeekBar previewAnimationSpeedBar;
    private SeekBar fullscreenAnimationSpeedBar;
    private TextView previewAnimationSpeedLabel;
    private TextView fullscreenAnimationSpeedLabel;
    private Button fullscreenActualRatioButton;
    private SeekBar fullscreenLineWidthBar;
    private TextView fullscreenLineWidthLabel;
    private TextView fullscreenPreviousView;
    private TextView fullscreenNumberView;
    private TextView fullscreenNextView;
    private TextView fullscreenStepView;
    private AlertDialog projectManagerDialog;
    private TextView projectManagerCurrentView;

    private TextView fileNameView;
    private TextView previousValueView;
    private TextView currentValueView;
    private TextView nextValueView;
    private TextView progressView;
    private Button playPauseButton;
    private Button previewOpenButton;
    private Button quickDelayButton;
    private Button quickRateButton;

    @Override
    protected void attachBaseContext(Context base) {
        String language = base.getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_LANGUAGE, "system");
        if (language == null || "system".equals(language)) {
            super.attachBaseContext(base);
            return;
        }
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(Locale.forLanguageTag(language));
        super.attachBaseContext(base.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        delayMs = prefs.getInt(KEY_DELAY, 2500);
        speechRate = prefs.getFloat(KEY_RATE, 0.82f);
        ttsVolumePercent = Math.max(0, Math.min(100,
                prefs.getInt(KEY_TTS_VOLUME_PERCENT, 100)));
        repeatTwice = prefs.getBoolean(KEY_REPEAT, false);
        volumeKeysEnabled = prefs.getBoolean(KEY_VOLUME_KEYS, false);
        phoneVibrationEnabled = prefs.getBoolean(KEY_PHONE_VIBRATION, false);
        volumeUpAction = prefs.getString(KEY_VOLUME_UP_ACTION, "previous");
        volumeDownAction = prefs.getString(KEY_VOLUME_DOWN_ACTION, "next");
        watchVibrationEnabled = prefs.getBoolean(KEY_WATCH_VIBRATION, true);
        watchKeepScreenOn = prefs.getBoolean(KEY_WATCH_KEEP_ON, true);
        watchOutwardAction = prefs.getString(KEY_WATCH_OUTWARD_ACTION, "next");
        watchInwardAction = prefs.getString(KEY_WATCH_INWARD_ACTION, "previous");
        mediaDuckingEnabled = prefs.getBoolean(KEY_MEDIA_DUCKING, true);
        wearableControlEnabled = prefs.getBoolean(KEY_WEARABLE_CONTROL, false);
        previewVisible = prefs.getBoolean(KEY_PREVIEW_VISIBLE, true);
        previewMinimized = prefs.getBoolean(KEY_PREVIEW_MIN, false);
        previewUseActualRatio = prefs.getBoolean(KEY_PREVIEW_USE_ACTUAL_RATIO, true);
        generatorNails = prefs.getInt(KEY_PROJECT_NAILS, generatorNails);
        generatorCircleMm = prefs.getInt(KEY_PROJECT_CIRCLE_MM, generatorCircleMm);
        generatorLineMm = prefs.getFloat(KEY_PROJECT_LINE_MM, generatorLineMm);
        generatorLineMm = Math.max(MIN_THREAD_MM, Math.min(MAX_THREAD_MM, generatorLineMm));
        // v4.1.7 changes custom preview width from fixed screen dp to a physical thread
        // diameter. The old dp preference is intentionally not reinterpreted as millimetres.
        previewCustomLineMm = prefs.getFloat(KEY_PREVIEW_CUSTOM_LINE_MM, generatorLineMm);
        previewCustomLineMm = Math.max(MIN_THREAD_MM, Math.min(MAX_THREAD_MM, previewCustomLineMm));
        generatorAutoStop = prefs.getBoolean(KEY_GENERATOR_AUTO_STOP, true);
        generatorVisualSpeed = Math.max(1, Math.min(20,
                prefs.getInt(KEY_GENERATOR_VISUAL_SPEED, 5)));
        previewAnimationSpeed = Math.max(1, Math.min(20,
                prefs.getInt(KEY_PREVIEW_ANIMATION_SPEED, 5)));
        restoreActiveProjectReference();

        landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        buildUi();
        restoreSequence();
        restoreCurrentThumbnailFromActiveProject();
        setupMediaSession();
        updateUi();
        if (wearableControlEnabled) connectWearable(false);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        tts = new TextToSpeech(this, this);
    }

    private void buildUi() {
        wearablePageVisible = false;
        wearableStatusView = null;
        root = new LinearLayout(this);
        root.setOrientation(landscape ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        root.setPadding(dp(10), dp(8), dp(10), dp(10));
        root.setBackgroundColor(BG);
        root.setFitsSystemWindows(true);

        mainPane = new LinearLayout(this);
        mainPane.setOrientation(LinearLayout.VERTICAL);
        mainPane.setGravity(Gravity.CENTER_HORIZONTAL);

        buildToolbar(mainPane);
        buildNumberArea(mainPane);
        buildControls(mainPane);

        if (landscape) {
            root.addView(mainPane, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        } else {
            root.addView(mainPane, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        }

        buildPreviewPanel();
        if (landscape) {
            root.addView(previewContainer, new LinearLayout.LayoutParams(
                    dp(previewMinimized ? 54 : 330), LinearLayout.LayoutParams.MATCH_PARENT));
        } else {
            root.addView(previewContainer, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(previewMinimized ? 48 : 250)));
        }

        setContentView(root);
        applyPreviewPanelState(false);
    }

    private void buildToolbar(LinearLayout parent) {
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER);
        toolbar.setPadding(dp(2), dp(2), dp(2), dp(2));
        toolbar.setBackground(roundedBackground(PANEL, 16));

        Button generateButton = makeButton("生成图片");
        styleAccentButton(generateButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showGeneratorEntry(); }
        });
        toolbar.addView(generateButton, toolbarButtonParams());

        Button importButton = makeButton("导入");
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openImportPicker(); }
        });
        toolbar.addView(importButton, toolbarButtonParams());

        previewOpenButton = makeButton("预览");
        previewOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (previewVisible && !previewMinimized) {
                    previewVisible = false;
                } else {
                    previewVisible = true;
                    previewMinimized = false;
                }
                applyPreviewPanelState(true);
            }
        });
        toolbar.addView(previewOpenButton, toolbarButtonParams());

        Button settingsButton = makeButton("更多");
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showMoreMenu(); }
        });
        toolbar.addView(settingsButton, toolbarButtonParams());

        quickSettingsRow = new LinearLayout(this);
        quickSettingsRow.setOrientation(LinearLayout.HORIZONTAL);
        quickSettingsRow.setGravity(Gravity.CENTER);
        quickSettingsRow.setPadding(dp(2), dp(4), dp(2), 0);

        Button quickSlowerButton = makeSmallActionButton("慢一点");
        quickSlowerButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                adjustSpeechDelay(QUICK_DELAY_STEP_MS, true);
            }
        });
        quickSettingsRow.addView(quickSlowerButton, quickSettingParams());

        quickDelayButton = makeSmallActionButton("");
        quickDelayButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showQuickDelayDialog(); }
        });
        quickSettingsRow.addView(quickDelayButton, quickSettingParams());

        Button quickFasterButton = makeSmallActionButton("快一点");
        quickFasterButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                adjustSpeechDelay(-QUICK_DELAY_STEP_MS, true);
            }
        });
        quickSettingsRow.addView(quickFasterButton, quickSettingParams());

        quickRateButton = makeSmallActionButton("");
        quickRateButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showQuickRateDialog(); }
        });
        quickSettingsRow.addView(quickRateButton, quickSettingParams());

        Button saveQuickButton = makeSmallActionButton("存档");
        saveQuickButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showSaveManager(true); }
        });
        quickSettingsRow.addView(saveQuickButton, quickSettingParams());

        if (landscape) {
            toolbar.setPadding(dp(2), dp(1), dp(2), dp(1));
            quickSettingsRow.setPadding(dp(2), dp(1), dp(2), dp(1));
            LinearLayout compactTop = new LinearLayout(this);
            compactTop.setOrientation(LinearLayout.HORIZONTAL);
            compactTop.setGravity(Gravity.CENTER_VERTICAL);
            compactTop.addView(toolbar, new LinearLayout.LayoutParams(
                    0, dp(42), 1.6f));
            LinearLayout.LayoutParams quickParams = new LinearLayout.LayoutParams(
                    0, dp(42), 1f);
            quickParams.setMargins(dp(4), 0, 0, 0);
            compactTop.addView(quickSettingsRow, quickParams);
            parent.addView(compactTop, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));
        } else {
            parent.addView(toolbar, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
            parent.addView(quickSettingsRow, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(38)));
        }

        fileNameView = new TextView(this);
        fileNameView.setTextColor(MUTED);
        fileNameView.setTextSize(landscape ? 10.5f : 12f);
        fileNameView.setSingleLine(true);
        fileNameView.setGravity(Gravity.CENTER);
        fileNameView.setPadding(dp(8), 0, dp(8), 0);
        parent.addView(fileNameView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(landscape ? 18 : 24)));
    }

    private LinearLayout.LayoutParams quickSettingParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                0, dp(landscape ? 38 : 32), 1f);
        p.setMargins(dp(2), 0, dp(2), 0);
        return p;
    }

    private Button makeSmallActionButton(String text) {
        Button button = makeButton(text);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setTextSize(landscape ? 9f : 10.5f);
        button.setPadding(dp(2), 0, dp(2), 0);
        return button;
    }

    private LinearLayout.LayoutParams toolbarButtonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                0, dp(landscape ? 38 : 44), 1f);
        p.setMargins(dp(2), dp(2), dp(2), dp(2));
        return p;
    }

    private void buildNumberArea(LinearLayout parent) {
        final boolean compactNumbers = landscape || useCompactPortraitNumbers();
        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.VERTICAL);
        display.setGravity(Gravity.CENTER);
        display.setPadding(dp(8), dp(compactNumbers ? 2 : 8), dp(8), dp(compactNumbers ? 2 : 4));
        display.setBackground(roundedBackground(PANEL, 18));

        previousValueView = makeNumberView(compactNumbers ? 32f : 46f, MUTED, false);
        previousValueView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { movePrevious(true); }
        });

        currentValueView = makeNumberView(compactNumbers ? 46f : 116f, FG, true);
        currentValueView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { replayCurrent(); }
        });
        currentValueView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                showJumpDialog();
                return true;
            }
        });

        nextValueView = makeNumberView(compactNumbers ? 32f : 46f, MUTED, false);
        nextValueView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveNextFromPhoneTap(); }
        });

        if (compactNumbers) {
            previousValueView.setAutoSizeTextTypeUniformWithConfiguration(
                    18, 36, 2, TypedValue.COMPLEX_UNIT_SP);
            currentValueView.setAutoSizeTextTypeUniformWithConfiguration(
                    24, 50, 2, TypedValue.COMPLEX_UNIT_SP);
            nextValueView.setAutoSizeTextTypeUniformWithConfiguration(
                    18, 36, 2, TypedValue.COMPLEX_UNIT_SP);
            LinearLayout numberRow = new LinearLayout(this);
            numberRow.setOrientation(LinearLayout.HORIZONTAL);
            numberRow.setGravity(Gravity.CENTER);
            addLandscapeNumberColumn(numberRow, "前一个", previousValueView);
            addLandscapeNumberColumn(numberRow, "当前", currentValueView);
            addLandscapeNumberColumn(numberRow, "下一个", nextValueView);
            display.addView(numberRow, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        } else {
            display.addView(makeSectionLabel("前一个"), new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(24)));
            display.addView(previousValueView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(58)));
            display.addView(makeSectionLabel("当前"), new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(24)));
            display.addView(currentValueView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
            display.addView(makeSectionLabel("下一个"), new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(24)));
            display.addView(nextValueView, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(58)));
        }

        LinearLayout progressRow = new LinearLayout(this);
        progressRow.setOrientation(LinearLayout.HORIZONTAL);
        progressRow.setGravity(Gravity.CENTER_VERTICAL);
        progressView = new TextView(this);
        progressView.setTextColor(MUTED);
        progressView.setTextSize(14f);
        progressView.setGravity(Gravity.CENTER);
        progressView.setMaxLines(landscape ? 1 : 2);
        progressView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showJumpDialog(); }
        });
        progressView.setTextSize(landscape ? 12f : 14f);
        progressRow.addView(progressView, new LinearLayout.LayoutParams(
                0, dp(landscape ? 28 : 42), 1f));

        Button recoverButton = makeButton("找回");
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showRecoverPositionDialog(); }
        });
        progressRow.addView(recoverButton, new LinearLayout.LayoutParams(
                dp(landscape ? 68 : 70), dp(landscape ? 28 : 40)));

        Button jumpButton = makeButton("跳转");
        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showJumpDialog(); }
        });
        progressRow.addView(jumpButton, new LinearLayout.LayoutParams(
                dp(landscape ? 68 : 70), dp(landscape ? 28 : 40)));
        display.addView(progressRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(landscape ? 30 : 44)));

        LinearLayout.LayoutParams displayParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        displayParams.setMargins(0, dp(landscape ? 3 : 6), 0, dp(landscape ? 2 : 4));
        parent.addView(display, displayParams);
    }

    private void addLandscapeNumberColumn(LinearLayout row, String title, TextView valueView) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        column.setPadding(dp(3), 0, dp(3), 0);
        column.addView(makeSectionLabel(title), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(18)));
        column.addView(valueView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        columnParams.setMargins(dp(2), 0, dp(2), 0);
        row.addView(column, columnParams);
    }

    private boolean useCompactPortraitNumbers() {
        if (landscape) return false;
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        return width > 0 && height > 0 && height / (float) width <= 1.90f;
    }

    private void buildControls(LinearLayout parent) {
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        controls.setPadding(dp(2), dp(2), dp(2), dp(2));
        controls.setBackground(roundedBackground(PANEL, 16));

        Button previousButton = makeButton("上一步");
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { movePrevious(true); }
        });
        controls.addView(previousButton, weightedButtonParams());

        Button replayButton = makeButton("重播");
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { replayCurrent(); }
        });
        controls.addView(replayButton, weightedButtonParams());

        playPauseButton = makeButton("播放");
        styleAccentButton(playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { togglePlayback(); }
        });
        controls.addView(playPauseButton, weightedButtonParams());

        Button nextButton = makeButton("下一步");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveNextFromPhoneTap(); }
        });
        controls.addView(nextButton, weightedButtonParams());

        parent.addView(controls, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(landscape ? 46 : 56)));

        TextView hint = new TextView(this);
        hint.setText(tr("点当前数字重播；长按当前数字或点“跳转”输入步骤"));
        hint.setTextColor(MUTED);
        hint.setTextSize(landscape ? 9.5f : 11f);
        hint.setGravity(Gravity.CENTER);
        parent.addView(hint, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(landscape ? 18 : 24)));
    }

    private void buildPreviewPanel() {
        previewContainer = new LinearLayout(this);
        previewContainer.setOrientation(LinearLayout.VERTICAL);
        previewContainer.setBackground(roundedBackground(PANEL, 18));
        LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        if (landscape) margin.setMargins(dp(8), 0, 0, 0);
        else margin.setMargins(0, dp(6), 0, 0);
        previewContainer.setLayoutParams(margin);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), 0, dp(4), 0);

        TextView title = new TextView(this);
        title.setText(tr("实时预览"));
        title.setTextColor(FG);
        title.setTextSize(14f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(44), 1f));

        Button minimize = makeCompactButton("—");
        minimize.setContentDescription("缩小预览");
        minimize.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                previewMinimized = !previewMinimized;
                applyPreviewPanelState(true);
            }
        });
        header.addView(minimize, new LinearLayout.LayoutParams(dp(46), dp(40)));

        Button full = makeCompactButton("⛶");
        full.setContentDescription("全屏预览");
        full.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showFullscreenPreview(); }
        });
        header.addView(full, new LinearLayout.LayoutParams(dp(46), dp(40)));

        Button close = makeCompactButton("×");
        close.setContentDescription("关闭预览");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                previewVisible = false;
                applyPreviewPanelState(true);
            }
        });
        header.addView(close, new LinearLayout.LayoutParams(dp(46), dp(40)));
        previewContainer.addView(header, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        previewBody = new LinearLayout(this);
        previewBody.setOrientation(LinearLayout.VERTICAL);
        previewBody.setPadding(dp(6), dp(2), dp(6), dp(6));
        previewBody.addView(buildPreviewAnimationControls(false), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42)));
        previewView = new StringArtPreview(this);
        previewBody.addView(previewView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView legend = new TextView(this);
        legend.setText(tr("黑色：已完成　紫色：当前正在绕的线"));
        legend.setTextColor(MUTED);
        legend.setTextSize(11f);
        legend.setGravity(Gravity.CENTER);
        previewBody.addView(legend, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(24)));
        previewContainer.addView(previewBody, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    private View buildPreviewAnimationControls(final boolean fullscreen) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        Button playAll = makeButton("完整动画");
        playAll.setTextSize(12f);
        playAll.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startFullPreviewAnimation(); }
        });
        row.addView(playAll, new LinearLayout.LayoutParams(
                fullscreen ? dp(132) : dp(104), dp(38)));

        TextView speedLabel = new TextView(this);
        speedLabel.setTextColor(FG);
        speedLabel.setTextSize(fullscreen ? 13f : 11f);
        speedLabel.setGravity(Gravity.CENTER);
        row.addView(speedLabel, new LinearLayout.LayoutParams(
                fullscreen ? dp(94) : dp(72), dp(38)));

        SeekBar speedBar = new SeekBar(this);
        speedBar.setMax(19);
        speedBar.setProgress(previewAnimationSpeed - 1);
        speedBar.setContentDescription(tr("动画速度"));
        speedBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                previewAnimationSpeed = progress + 1;
                if (fromUser) prefs.edit()
                        .putInt(KEY_PREVIEW_ANIMATION_SPEED, previewAnimationSpeed).apply();
                syncPreviewAnimationSpeedControls(seekBar);
            }
        });
        row.addView(speedBar, new LinearLayout.LayoutParams(0, dp(38), 1f));
        if (fullscreen) {
            fullscreenAnimationSpeedBar = speedBar;
            fullscreenAnimationSpeedLabel = speedLabel;
        } else {
            previewAnimationSpeedBar = speedBar;
            previewAnimationSpeedLabel = speedLabel;
        }
        syncPreviewAnimationSpeedControls(null);
        return row;
    }

    private View buildPreviewLineWidthControls() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        final Button actualRatioButton = makeButton("实际比例");
        actualRatioButton.setTextSize(12f);
        row.addView(actualRatioButton, new LinearLayout.LayoutParams(dp(104), dp(40)));

        final TextView lineWidthLabel = new TextView(this);
        lineWidthLabel.setTextColor(FG);
        lineWidthLabel.setTextSize(12f);
        lineWidthLabel.setGravity(Gravity.CENTER);
        row.addView(lineWidthLabel, new LinearLayout.LayoutParams(dp(104), dp(40)));

        final SeekBar lineWidthBar = new SeekBar(this);
        lineWidthBar.setMax(99); // 0.01–1.00 mm, every 0.01 mm
        lineWidthBar.setProgress(previewLineWidthProgress());
        lineWidthBar.setContentDescription(tr("预览线径"));
        row.addView(lineWidthBar, new LinearLayout.LayoutParams(0, dp(40), 1f));

        final Runnable refreshLabel = new Runnable() {
            @Override public void run() {
                syncPreviewLineWidthControls();
            }
        };
        lineWidthBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                previewUseActualRatio = false;
                previewCustomLineMm = MIN_THREAD_MM + progress / 100f;
                persistPreviewLineWidth();
                refreshLabel.run();
                updatePreviewViews();
            }
        });
        actualRatioButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                previewUseActualRatio = true;
                previewCustomLineMm = generatorLineMm;
                lineWidthBar.setProgress(previewLineWidthProgress());
                persistPreviewLineWidth();
                refreshLabel.run();
                updatePreviewViews();
            }
        });
        fullscreenActualRatioButton = actualRatioButton;
        fullscreenLineWidthBar = lineWidthBar;
        fullscreenLineWidthLabel = lineWidthLabel;
        refreshLabel.run();
        return row;
    }

    private int previewLineWidthProgress() {
        float shown = previewUseActualRatio ? generatorLineMm : previewCustomLineMm;
        return Math.max(0, Math.min(99,
                Math.round((shown - MIN_THREAD_MM) * 100f)));
    }

    private void syncPreviewLineWidthControls() {
        if (fullscreenLineWidthBar != null)
            fullscreenLineWidthBar.setProgress(previewLineWidthProgress());
        if (fullscreenLineWidthLabel != null) {
            float shown = previewUseActualRatio ? generatorLineMm : previewCustomLineMm;
            fullscreenLineWidthLabel.setText((isEnglish() ? "Thread " : "线径 ")
                    + formatLineWidthMm(shown));
        }
        if (fullscreenActualRatioButton != null)
            fullscreenActualRatioButton.setAlpha(previewUseActualRatio ? 1f : .72f);
    }

    private void syncPreviewAnimationSpeedControls(SeekBar source) {
        String text = tr("速度") + " " + previewAnimationSpeed + "×";
        if (previewAnimationSpeedLabel != null) previewAnimationSpeedLabel.setText(text);
        if (fullscreenAnimationSpeedLabel != null) fullscreenAnimationSpeedLabel.setText(text);
        if (previewAnimationSpeedBar != null && previewAnimationSpeedBar != source)
            previewAnimationSpeedBar.setProgress(previewAnimationSpeed - 1);
        if (fullscreenAnimationSpeedBar != null && fullscreenAnimationSpeedBar != source)
            fullscreenAnimationSpeedBar.setProgress(previewAnimationSpeed - 1);
    }

    private Button makeCompactButton(String text) {
        Button b = makeButton(text);
        b.setTextSize(20f);
        b.setPadding(0, 0, 0, 0);
        return b;
    }

    private void applyPreviewPanelState(boolean persist) {
        if (previewContainer == null) return;
        boolean wasVisible = previewContainer.getVisibility() == View.VISIBLE;
        boolean animate = persist && root != null;
        previewOpenButton.setText(tr(previewVisible && !previewMinimized ? "关预览" : "预览"));

        ViewGroup.LayoutParams raw = previewContainer.getLayoutParams();
        if (raw instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) raw;
            int oldSize = landscape ? p.width : p.height;
            int targetSize = dp(previewMinimized ? (landscape ? 54 : 48) : (landscape ? 330 : 250));
            if (landscape) {
                p.width = targetSize;
                p.height = LinearLayout.LayoutParams.MATCH_PARENT;
            } else {
                p.width = LinearLayout.LayoutParams.MATCH_PARENT;
                p.height = targetSize;
            }
            previewContainer.setLayoutParams(p);
            if (previewVisible && animate && oldSize > 0 && oldSize != targetSize) {
                animatePreviewSize(oldSize, targetSize);
            }
        }
        if (previewVisible) {
            previewContainer.setVisibility(View.VISIBLE);
            if (!wasVisible && animate) {
                previewContainer.setAlpha(0f);
                previewContainer.setTranslationY(landscape ? 0f : dp(10));
                previewContainer.animate().alpha(1f).translationY(0f).setDuration(190)
                        .setInterpolator(new DecelerateInterpolator()).start();
            } else previewContainer.setAlpha(1f);
            previewBody.setVisibility(previewMinimized ? View.GONE : View.VISIBLE);
        } else if (wasVisible && animate) {
            previewContainer.animate().alpha(0f).translationY(landscape ? 0f : dp(8)).setDuration(150)
                    .withEndAction(new Runnable() {
                        @Override public void run() {
                            previewContainer.setVisibility(View.GONE);
                            previewContainer.setAlpha(1f);
                            previewContainer.setTranslationY(0f);
                        }
                    }).start();
        } else previewContainer.setVisibility(View.GONE);
        if (root != null) root.requestLayout();
        if (currentValueView != null) currentValueView.requestLayout();
        if (persist) {
            prefs.edit().putBoolean(KEY_PREVIEW_VISIBLE, previewVisible)
                    .putBoolean(KEY_PREVIEW_MIN, previewMinimized).apply();
        }
        updatePreviewViews();
    }

    private void animatePreviewSize(final int from, final int to) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(220);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams raw = previewContainer.getLayoutParams();
                if (!(raw instanceof LinearLayout.LayoutParams)) return;
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) raw;
                if (landscape) p.width = (Integer) valueAnimator.getAnimatedValue();
                else p.height = (Integer) valueAnimator.getAnimatedValue();
                previewContainer.setLayoutParams(p);
            }
        });
        animator.start();
    }

    private void showFullscreenPreview() {
        if (fullscreenDialog != null && fullscreenDialog.isShowing()) return;
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        // Fullscreen dialogs do not automatically respect display cutouts.
        panel.setPadding(dp(8), dp(34), dp(8), dp(8));
        panel.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText(tr("实时预览 · 紫色为当前线段"));
        title.setTextColor(FG);
        title.setTextSize(15f);
        top.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1f));

        Button min = makeButton("缩小");
        min.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                previewVisible = true;
                previewMinimized = true;
                dialog.dismiss();
                applyPreviewPanelState(true);
            }
        });
        top.addView(min, new LinearLayout.LayoutParams(dp(76), dp(44)));

        Button exit = makeButton("退出全屏");
        exit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialog.dismiss(); }
        });
        top.addView(exit, new LinearLayout.LayoutParams(dp(96), dp(44)));

        Button close = makeButton("关闭");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                previewVisible = false;
                dialog.dismiss();
                applyPreviewPanelState(true);
            }
        });
        top.addView(close, new LinearLayout.LayoutParams(dp(72), dp(44)));
        panel.addView(top, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));

        fullscreenPreview = new StringArtPreview(this);
        panel.addView(fullscreenPreview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        panel.addView(buildPreviewLineWidthControls(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        panel.addView(buildPreviewAnimationControls(true), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER_VERTICAL);

        Button previousStep = makeButton("上一步");
        previousStep.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { movePrevious(true); }
        });
        navigation.addView(previousStep, new LinearLayout.LayoutParams(0, dp(58), 1f));

        LinearLayout numberBlock = new LinearLayout(this);
        numberBlock.setOrientation(LinearLayout.VERTICAL);
        numberBlock.setGravity(Gravity.CENTER);
        LinearLayout numberRow = new LinearLayout(this);
        numberRow.setOrientation(LinearLayout.HORIZONTAL);
        numberRow.setGravity(Gravity.CENTER);

        fullscreenPreviousView = addFullscreenNumberColumn(
                numberRow, "前一个", 19f, MUTED, false, .85f);
        fullscreenNumberView = addFullscreenNumberColumn(
                numberRow, "当前", 28f, FG, true, 1f);
        fullscreenNextView = addFullscreenNumberColumn(
                numberRow, "下一个", 19f, MUTED, false, .85f);
        fullscreenStepView = makeSectionLabel("");
        numberBlock.addView(numberRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        numberBlock.addView(fullscreenStepView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(20)));
        navigation.addView(numberBlock, new LinearLayout.LayoutParams(0, dp(64),
                landscape ? 1.65f : 1.8f));

        Button jump = makeButton("跳转");
        jump.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showJumpDialog(); }
        });
        navigation.addView(jump, new LinearLayout.LayoutParams(0, dp(58), .8f));

        Button nextStep = makeButton("下一步");
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveNextFromPhoneTap(); }
        });
        navigation.addView(nextStep, new LinearLayout.LayoutParams(0, dp(58), 1f));
        panel.addView(navigation, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(66)));

        dialog.setContentView(panel);
        Window w = dialog.getWindow();
        if (w != null) w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface d) {
                fullscreenPreview = null;
                fullscreenDialog = null;
                fullscreenAnimationSpeedBar = null;
                fullscreenAnimationSpeedLabel = null;
                fullscreenActualRatioButton = null;
                fullscreenLineWidthBar = null;
                fullscreenLineWidthLabel = null;
                fullscreenPreviousView = null;
                fullscreenNumberView = null;
                fullscreenNextView = null;
                fullscreenStepView = null;
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override public boolean onKey(DialogInterface d, int keyCode, KeyEvent event) {
                return previewAnimationRunning && keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        fullscreenDialog = dialog;
        dialog.show();
        updatePreviewViews();
    }

    private TextView addFullscreenNumberColumn(LinearLayout row, String title, float sizeSp,
                                               int color, boolean bold, float weight) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        TextView label = makeSectionLabel(title);
        label.setTextSize(landscape ? 11f : 9f);
        TextView value = makeNumberView(sizeSp, color, bold);
        column.addView(label, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(16)));
        column.addView(value, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        row.addView(column, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, weight));
        return value;
    }

    private void updatePreviewViews() {
        syncPreviewLineWidthControls();
        if (previewView != null) previewView.invalidate();
        if (fullscreenPreview != null) fullscreenPreview.invalidate();
        updateFullscreenStepDisplay();
    }

    private int previewRenderIndex() {
        if (sequence.isEmpty()) return 0;
        int index = (previewAnimationRunning || previewAnimationResultHeld)
                && previewAnimationIndex >= 0
                ? previewAnimationIndex : currentIndex;
        return Math.max(0, Math.min(index, sequence.size() - 1));
    }

    private void startFullPreviewAnimation() {
        if (previewAnimationRunning) return;
        releaseHeldPreviewResult();
        if (sequence.isEmpty()) {
            toast("请先导入 TXT 序列", Toast.LENGTH_SHORT);
            return;
        }
        previewAnimationWasPlaying = isPlaying;
        if (isPlaying) pausePlayback();
        previewAnimationRunning = true;
        previewAnimationHolding = false;
        previewAnimationIndex = Math.max(0, Math.min(currentIndex, sequence.size() - 1));
        lockControlsForPreviewAnimation();
        updatePreviewViews();

        previewAnimationRunnable = new Runnable() {
            @Override public void run() {
                if (!previewAnimationRunning || sequence.isEmpty()) return;
                int last = sequence.size() - 1;
                if (previewAnimationIndex >= last) {
                    previewAnimationIndex = last;
                    updatePreviewViews();
                    if (!previewAnimationHolding) {
                        previewAnimationHolding = true;
                        previewAnimationHandler.postDelayed(new Runnable() {
                            @Override public void run() {
                                finishFullPreviewAnimation(true);
                            }
                        }, 1000L);
                    }
                    return;
                }
                previewAnimationIndex = Math.min(last,
                        previewAnimationIndex + Math.max(1, previewAnimationSpeed));
                updatePreviewViews();
                previewAnimationHandler.postDelayed(this, 32L);
            }
        };
        previewAnimationHandler.postDelayed(previewAnimationRunnable, 32L);
    }

    private void finishFullPreviewAnimation(boolean restorePlayback) {
        if (!previewAnimationRunning) return;
        boolean shouldResume = restorePlayback && previewAnimationWasPlaying
                && !activityDestroyed && !sequence.isEmpty();
        previewAnimationHandler.removeCallbacksAndMessages(null);
        previewAnimationRunning = false;
        previewAnimationHolding = false;
        previewAnimationResultHeld = restorePlayback;
        if (!previewAnimationResultHeld) previewAnimationIndex = -1;
        previewAnimationRunnable = null;
        previewAnimationWasPlaying = false;
        unlockControlsAfterPreviewAnimation();
        updateUi();
        if (shouldResume && ttsReady) {
            isPlaying = true;
            playGeneration++;
            updateUi();
            speakCurrent(true);
        }
    }

    private void releaseHeldPreviewResult() {
        if (!previewAnimationResultHeld) return;
        previewAnimationResultHeld = false;
        previewAnimationIndex = -1;
        updatePreviewViews();
    }

    private void lockControlsForPreviewAnimation() {
        previewAnimationLockedViews.clear();
        previewAnimationLockedEnabled.clear();
        previewAnimationLockedAlpha.clear();
        lockControlsRecursively(root);
        if (fullscreenDialog != null && fullscreenDialog.getWindow() != null)
            lockControlsRecursively(fullscreenDialog.getWindow().getDecorView());
        if (previewAnimationSpeedBar != null) {
            previewAnimationSpeedBar.setEnabled(true);
            previewAnimationSpeedBar.setAlpha(1f);
        }
        if (fullscreenAnimationSpeedBar != null) {
            fullscreenAnimationSpeedBar.setEnabled(true);
            fullscreenAnimationSpeedBar.setAlpha(1f);
        }
    }

    private void lockControlsRecursively(View view) {
        if (view == null || view == previewAnimationSpeedBar
                || view == fullscreenAnimationSpeedBar) return;
        if (view instanceof Button || view instanceof SeekBar
                || view instanceof EditText || view instanceof CheckBox) {
            previewAnimationLockedViews.add(view);
            previewAnimationLockedEnabled.add(view.isEnabled());
            previewAnimationLockedAlpha.add(view.getAlpha());
            view.setEnabled(false);
            view.setAlpha(.36f);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++)
                lockControlsRecursively(group.getChildAt(i));
        }
    }

    private void unlockControlsAfterPreviewAnimation() {
        int count = Math.min(previewAnimationLockedViews.size(),
                Math.min(previewAnimationLockedEnabled.size(), previewAnimationLockedAlpha.size()));
        for (int i = 0; i < count; i++) {
            View view = previewAnimationLockedViews.get(i);
            view.setEnabled(previewAnimationLockedEnabled.get(i));
            view.setAlpha(previewAnimationLockedAlpha.get(i));
        }
        previewAnimationLockedViews.clear();
        previewAnimationLockedEnabled.clear();
        previewAnimationLockedAlpha.clear();
    }

    private void updateFullscreenStepDisplay() {
        if (fullscreenNumberView == null || fullscreenStepView == null
                || fullscreenPreviousView == null || fullscreenNextView == null) return;
        if (sequence.isEmpty()) {
            fullscreenPreviousView.setText("—");
            fullscreenNumberView.setText("—");
            fullscreenNextView.setText("—");
            fullscreenStepView.setText(tr("尚未导入序列"));
            return;
        }
        int index = previewRenderIndex();
        fullscreenPreviousView.setText(index > 0
                ? String.valueOf(sequence.get(index - 1)) : "—");
        fullscreenNumberView.setText(String.valueOf(sequence.get(index)));
        fullscreenNextView.setText(index + 1 < sequence.size()
                ? String.valueOf(sequence.get(index + 1)) : "—");
        fullscreenStepView.setText(tr("第 " + (index + 1) + " / " + sequence.size() + " 步"));
    }

    private LinearLayout.LayoutParams weightedButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, dp(landscape ? 40 : 50), 1f);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        return params;
    }

    private TextView makeSectionLabel(String text) {
        TextView view = new TextView(this);
        view.setText(tr(text));
        view.setTextColor(MUTED);
        view.setTextSize(13f);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private TextView makeNumberView(float sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setTextColor(color);
        view.setTextSize(sizeSp);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private Button makeButton(CharSequence text) {
        Button button = new Button(this);
        button.setText(text instanceof android.text.Spanned ? text : tr(String.valueOf(text)));
        button.setTextSize(13f);
        button.setTextColor(FG);
        button.setAllCaps(false);
        styleButton(button, PANEL_2, PANEL);
        button.setPadding(dp(3), 0, dp(3), 0);
        return button;
    }

    private GradientDrawable roundedBackground(int color, int radiusDp) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(radiusDp));
        return background;
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private void styleButton(final Button button, int color, int rippleColor) {
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(rippleColor),
                roundedBackground(color, 12), null));
        button.setElevation(dp(1));
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    releaseHeldPreviewResult();
                    button.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70).start();
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                        || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    button.animate().scaleX(1f).scaleY(1f).setDuration(120)
                            .setInterpolator(new OvershootInterpolator(1.6f)).start();
                }
                return false;
            }
        });
    }

    private void styleAccentButton(Button button) {
        button.setTextColor(Color.WHITE);
        styleButton(button, ACCENT, ACCENT_DARK);
        button.setElevation(dp(3));
    }

    private void openImportPicker() {
        pausePlayback();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_OPEN_TXT);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_OPEN_IMAGE);
    }

    private void openSavePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
                "application/octet-stream", "application/x-stringart-save"
        });
        try {
            startActivityForResult(intent, REQUEST_OPEN_SAVE);
        } catch (ActivityNotFoundException e) {
            intent.removeExtra(Intent.EXTRA_MIME_TYPES);
            startActivityForResult(intent, REQUEST_OPEN_SAVE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            if (requestCode == REQUEST_CREATE_SAVE) pendingSaveExportFile = null;
            return;
        }
        Uri uri = data.getData();
        if (uri == null) return;
        if (requestCode == REQUEST_CREATE_TEMPLATE_PDF) {
            try {
                writeTemplatePdf(uri, pendingTemplateNails, pendingTemplateDiameterMm);
                toast("钉位模板 PDF 已保存", Toast.LENGTH_LONG);
            } catch (IOException e) {
                showError(isEnglish()
                        ? "Could not save PDF: " + safeMessage(e)
                        : "保存 PDF 失败：" + safeMessage(e));
            }
            return;
        }
        if (requestCode == REQUEST_CREATE_SEQUENCE_TXT) {
            try {
                writeSequenceTxt(uri,
                        pendingExportSequence == null ? sequence : pendingExportSequence,
                        pendingExportNails, pendingExportLineMm, pendingExportCircleMm);
                toast("TXT 序列已导出", Toast.LENGTH_LONG);
            } catch (IOException e) {
                showError("导出 TXT 失败：" + safeMessage(e));
            }
            pendingExportSequence = null;
            pendingExportNails = 0;
            pendingExportLineMm = 0f;
            pendingExportCircleMm = 0;
            return;
        }
        if (requestCode == REQUEST_CREATE_SAVE) {
            File source = pendingSaveExportFile;
            pendingSaveExportFile = null;
            if (source == null || !source.isFile()) {
                showError("要导出的存档已不存在");
                return;
            }
            try {
                copyFileToUri(source, uri);
                toast("存档已导出，可在其他设备的项目管理中导入", Toast.LENGTH_LONG);
            } catch (IOException e) {
                showError("导出存档失败：" + safeMessage(e));
            }
            return;
        }
        if (requestCode == REQUEST_OPEN_SAVE) {
            importSaveFromUri(uri, true);
            return;
        }
        if (requestCode == REQUEST_OPEN_IMAGE) {
            try {
                pendingGeneratorBitmap = decodeGeneratorBitmap(uri);
                pendingGeneratorName = stripFileExtension(queryFileName(uri));
                generatorCropX = .5f;
                generatorCropY = .5f;
                generatorCropZoom = StringArtGenerator.minimumCropZoom(pendingGeneratorBitmap);
                showCropFullscreen();
            } catch (IOException e) {
                showError("读取图片失败：" + safeMessage(e));
            } catch (RuntimeException e) {
                showError("图片无法使用：" + safeMessage(e));
            }
            return;
        }
        if (requestCode != REQUEST_OPEN_TXT) return;
        String selectedName = queryFileName(uri);
        String lowerName = selectedName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".bin") || lowerName.endsWith(".sar")) {
            importSaveFromUri(uri, true);
            return;
        }
        if (!lowerName.endsWith(".txt")) {
            showError(isEnglish()
                    ? "Unsupported file type. Please choose a .txt sequence or .bin save file."
                    : "不支持这种文件格式，请选择 .txt 序列或 .bin 存档文件。");
            return;
        }
        try {
            String name = selectedName;
            String text = readText(uri);
            ArrayList<Integer> parsed = parseSequence(text);
            if (parsed.size() < 2) {
                showError("没有识别到有效的钉号序列。TXT 可使用换行、空格、逗号或 0 → 87 格式。");
                return;
            }
            if (parsed.size() > MAX_SEQUENCE_LENGTH) {
                showError("识别到的数字过多（" + parsed.size() + " 个）。");
                return;
            }
            TxtMetadata metadata = parseTxtMetadata(name, text, parsed);
            showImportPreview(parsed, name, metadata.nails, metadata.lineMm,
                    metadata.circleMm);
        } catch (IOException e) {
            showError("读取文件失败：" + safeMessage(e));
        } catch (RuntimeException e) {
            showError("解析文件失败：" + safeMessage(e));
        }
    }

    private String readText(Uri uri) throws IOException {
        ContentResolver resolver = getContentResolver();
        InputStream input = resolver.openInputStream(uri);
        if (input == null) throw new IOException("无法打开文件");
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_FILE_BYTES) throw new IOException("文件超过 8 MB");
                output.write(buffer, 0, read);
            }
            byte[] bytes = output.toByteArray();
            if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE)
                return new String(bytes, 2, bytes.length - 2, Charset.forName("UTF-16LE"));
            if (bytes.length >= 2 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF)
                return new String(bytes, 2, bytes.length - 2, Charset.forName("UTF-16BE"));
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB
                    && bytes[2] == (byte) 0xBF)
                return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
            return new String(bytes, StandardCharsets.UTF_8);
        } finally {
            input.close();
        }
    }

    private void copyFileToUri(File source, Uri destination) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream(source));
        OutputStream output = getContentResolver().openOutputStream(destination, "w");
        if (output == null) {
            input.close();
            throw new IOException("无法写入所选位置");
        }
        try {
            copyStream(input, output, Integer.MAX_VALUE);
            output.flush();
        } finally {
            try { input.close(); } finally { output.close(); }
        }
    }

    private int copyStream(InputStream input, OutputStream output, int maximumBytes)
            throws IOException {
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > maximumBytes) throw new IOException("文件过大");
            output.write(buffer, 0, read);
        }
        return total;
    }

    private ArrayList<Integer> parseSequence(String text) {
        String normalized = text.replace('\uFEFF', ' ').replace('，', ',').replace('；', ';');
        String[] lines = normalized.split("\\r?\\n");
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (String sourceLine : lines) {
            String line = sourceLine.trim();
            if (line.length() == 0 || line.startsWith("#") || line.startsWith("//")) continue;
            Matcher stepPrefix = STEP_PREFIX_PATTERN.matcher(line);
            if (stepPrefix.matches()) line = stepPrefix.group(1).trim();
            String lower = line.toLowerCase(Locale.ROOT);
            boolean hasRouteSeparator = line.contains("→") || line.contains("->")
                    || line.contains("=>") || line.contains(",") || line.contains(";")
                    || lower.contains(" to ") || line.contains(" 到 ") || line.contains("至");
            boolean onlyNumericSyntax = line.matches("[\\d\\s\\[\\]{}(),;|:：>→\\-+.]+");
            boolean metadataLine = containsMetadataWord(lower);
            ArrayList<Integer> numbers = extractIntegers(line);
            if (numbers.isEmpty()) continue;
            if (numbers.size() == 1 && line.matches("\\s*\\d+\\s*")) result.add(numbers.get(0));
            else if (numbers.size() >= 2 && (hasRouteSeparator || onlyNumericSyntax || !metadataLine))
                result.addAll(numbers);
        }
        if (result.size() < 2) result = extractIntegers(normalized);
        if (result.size() >= 3) {
            int first = result.get(0);
            if (first == result.size() - 1) result.remove(0);
            else if (first >= 1000) {
                int small = 0;
                int sample = Math.min(200, result.size() - 1);
                for (int i = 1; i <= sample; i++) if (result.get(i) <= 999) small++;
                if (sample > 0 && small >= sample * 0.95) result.remove(0);
            }
        }
        ArrayList<Integer> clean = new ArrayList<Integer>(result.size());
        for (Integer value : result)
            if (value != null && value >= 0 && value <= 99999) clean.add(value);
        return clean;
    }

    private TxtMetadata parseTxtMetadata(String fileName, String text,
                                         ArrayList<Integer> values) {
        int inferredNails = inferNailCount(values);
        String header = text == null ? "" : text.substring(0, Math.min(4096, text.length()));
        String metadataText = (fileName == null ? "" : fileName) + "\n" + header;

        int declaredNails = 0;
        Matcher nailsMatcher = NAILS_METADATA_PATTERN.matcher(metadataText);
        if (nailsMatcher.find()) {
            String value = nailsMatcher.group(1) != null
                    ? nailsMatcher.group(1) : nailsMatcher.group(2);
            try { declaredNails = Integer.parseInt(value); }
            catch (NumberFormatException ignored) { }
        }
        int plausibleMaximum = Math.max(500, inferredNails * 4);
        int nails = declaredNails >= inferredNails
                && declaredNails <= Math.min(10_000, plausibleMaximum)
                ? declaredNails : inferredNails;

        float lineMm = DEFAULT_IMPORTED_THREAD_MM;
        Matcher lineMatcher = THREAD_METADATA_PATTERN.matcher(metadataText);
        if (lineMatcher.find()) {
            try {
                float parsed = Float.parseFloat(lineMatcher.group(1));
                if (Float.isFinite(parsed)
                        && parsed >= MIN_THREAD_MM && parsed <= MAX_THREAD_MM)
                    lineMm = parsed;
            } catch (NumberFormatException ignored) { }
        }

        int circleMm = DEFAULT_IMPORTED_CIRCLE_MM;
        Matcher circleMatcher = CIRCLE_METADATA_PATTERN.matcher(metadataText);
        if (circleMatcher.find()) {
            try {
                float parsed = Float.parseFloat(circleMatcher.group(1));
                int rounded = Math.round(parsed);
                if (Float.isFinite(parsed)
                        && rounded >= MIN_CIRCLE_MM && rounded <= MAX_CIRCLE_MM)
                    circleMm = rounded;
            } catch (NumberFormatException ignored) { }
        }
        return new TxtMetadata(nails, lineMm, circleMm);
    }

    private int inferNailCount(ArrayList<Integer> values) {
        int max = 0;
        if (values != null) for (int value : values) max = Math.max(max, value);
        return Math.max(2, max + 1);
    }

    private int resolvedProjectNails(ArrayList<Integer> values, int declaredNails) {
        int inferred = inferNailCount(values);
        int plausibleMaximum = Math.max(500, inferred * 4);
        return declaredNails >= inferred
                && declaredNails <= Math.min(10_000, plausibleMaximum)
                ? declaredNails : inferred;
    }

    private boolean containsMetadataWord(String lower) {
        return lower.contains("count") || lower.contains("total") || lower.contains("weight")
                || lower.contains("maximum") || lower.contains("max line")
                || lower.contains("connection") || lower.contains("number of nails")
                || lower.contains("number of pins") || lower.contains("thread:")
                || lower.contains("rgb") || lower.contains("钉子数")
                || lower.contains("连接数") || lower.contains("线路数")
                || lower.contains("线重") || lower.contains("总数")
                || lower.contains("最大线路");
    }

    private ArrayList<Integer> extractIntegers(String value) {
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        Matcher matcher = INTEGER_PATTERN.matcher(value);
        while (matcher.find() && numbers.size() <= MAX_SEQUENCE_LENGTH) {
            try { numbers.add(Integer.parseInt(matcher.group())); }
            catch (NumberFormatException ignored) { }
        }
        return numbers;
    }

    private void showImportPreview(final ArrayList<Integer> parsed, final String name,
                                   int nails, float lineMm, int circleMm) {
        showSequencePreview(parsed, nails, name, lineMm, circleMm);
    }

    private String queryFileName(Uri uri) {
        String fallback = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String value = cursor.getString(index);
                    if (value != null && value.trim().length() > 0) return value;
                }
            }
        } catch (RuntimeException ignored) { }
        finally { if (cursor != null) cursor.close(); }
        try {
            String pathName = uri.getLastPathSegment();
            if (pathName != null && pathName.trim().length() > 0) {
                pathName = Uri.decode(pathName);
                int separator = Math.max(pathName.lastIndexOf('/'), pathName.lastIndexOf(':'));
                fallback = separator >= 0 ? pathName.substring(separator + 1) : pathName;
            }
        } catch (RuntimeException ignored) { }
        return fallback == null || fallback.trim().length() == 0
                ? "绕线序列.txt" : fallback;
    }

    private void updateUi() {
        boolean loaded = !sequence.isEmpty();
        fileNameView.setText(localizedProjectLabel(importedFileName));
        playPauseButton.setText(tr(isPlaying ? "暂停" : "播放"));
        updateQuickSettingsLabels();
        if (!loaded) {
            previousValueView.setText("—");
            currentValueView.setText(tr("导入"));
            applyCurrentNumberSizing(false);
            nextValueView.setText("—");
            progressView.setText(tr("尚未导入序列"));
            updatePreviewViews();
            updateMediaSessionState();
            sendWearableState(false);
            return;
        }
        currentIndex = Math.max(0, Math.min(currentIndex, sequence.size() - 1));
        applyCurrentNumberSizing(true);
        previousValueView.setText(currentIndex > 0 ? String.valueOf(sequence.get(currentIndex - 1)) : "—");
        currentValueView.setText(String.valueOf(sequence.get(currentIndex)));
        nextValueView.setText(currentIndex + 1 < sequence.size()
                ? String.valueOf(sequence.get(currentIndex + 1)) : "—");
        progressView.setText(progressAndEstimatedTimeText());
        if (pendingStepAnimation != 0) {
            animateStepChange(pendingStepAnimation);
            pendingStepAnimation = 0;
        }
        updatePreviewViews();
        updateMediaSessionState();
        sendWearableState(false);
    }

    @android.annotation.SuppressLint("SetTextI18n")
    private void updateQuickSettingsLabels() {
        if (quickDelayButton != null) quickDelayButton.setText(tr("间隔 ") + formatSeconds(delayMs));
        if (quickRateButton != null) quickRateButton.setText(tr("语速 ")
                + String.format(Locale.getDefault(), "%.2f×", speechRate));
    }

    private void animateStepChange(int direction) {
        if (currentValueView == null) return;
        float shift = dp(9) * (direction > 0 ? 1f : -1f);
        currentValueView.animate().cancel();
        currentValueView.setAlpha(0.25f);
        currentValueView.setScaleX(0.82f);
        currentValueView.setScaleY(0.82f);
        currentValueView.setTranslationY(shift);
        AnimatorSet numberAnimation = new AnimatorSet();
        numberAnimation.playTogether(
                ObjectAnimator.ofFloat(currentValueView, View.ALPHA, 0.25f, 1f),
                ObjectAnimator.ofFloat(currentValueView, View.SCALE_X, 0.82f, 1.05f, 1f),
                ObjectAnimator.ofFloat(currentValueView, View.SCALE_Y, 0.82f, 1.05f, 1f),
                ObjectAnimator.ofFloat(currentValueView, View.TRANSLATION_Y, shift, 0f));
        numberAnimation.setDuration(240);
        numberAnimation.setInterpolator(new DecelerateInterpolator());
        numberAnimation.start();

        animateSideNumber(previousValueView, -shift * 0.45f);
        animateSideNumber(nextValueView, shift * 0.45f);
    }

    private void animateSideNumber(TextView value, float shift) {
        if (value == null) return;
        value.animate().cancel();
        value.setAlpha(0.45f);
        value.setTranslationY(shift);
        value.animate().alpha(1f).translationY(0f).setDuration(180)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private void setupMediaSession() {
        mediaSession = new MediaSession(this, "StringArtReaderBluetoothControls");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() {
                if (!isPlaying) togglePlayback();
            }

            @Override public void onPause() {
                if (isPlaying) pausePlayback();
            }

            @Override public void onStop() {
                if (isPlaying) pausePlayback();
            }

            @Override public void onSkipToNext() {
                moveNext(true);
            }

            @Override public void onSkipToPrevious() {
                movePrevious(true);
            }
        }, handler);
        mediaSession.setActive(true);
        updateMediaSessionState();
    }

    private void updateMediaSessionState() {
        if (mediaSession == null) return;
        long actions = PlaybackState.ACTION_PLAY
                | PlaybackState.ACTION_PAUSE
                | PlaybackState.ACTION_PLAY_PAUSE
                | PlaybackState.ACTION_SKIP_TO_NEXT
                | PlaybackState.ACTION_SKIP_TO_PREVIOUS
                | PlaybackState.ACTION_STOP;
        int state;
        if (sequence.isEmpty()) state = PlaybackState.STATE_NONE;
        else state = isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        mediaSession.setPlaybackState(new PlaybackState.Builder()
                .setActions(actions)
                .setState(state, sequence.isEmpty() ? 0 : currentIndex,
                        isPlaying ? 1f : 0f)
                .build());
    }

    private void applyCurrentNumberSizing(boolean loaded) {
        currentValueView.setMaxLines(loaded ? 1 : 2);
        if (landscape) {
            currentValueView.setAutoSizeTextTypeUniformWithConfiguration(
                    loaded ? 22 : 18,
                    loaded ? 46 : 34,
                    2,
                    TypedValue.COMPLEX_UNIT_SP);
        } else {
            currentValueView.setAutoSizeTextTypeUniformWithConfiguration(
                    loaded ? 42 : 28,
                    loaded ? 116 : 52,
                    2,
                    TypedValue.COMPLEX_UNIT_SP);
        }
    }

    private void togglePlayback() {
        if (previewAnimationRunning) return;
        releaseHeldPreviewResult();
        if (sequence.isEmpty()) { toast("请先导入 TXT 序列", Toast.LENGTH_SHORT); return; }
        if (!ttsReady) { toast("系统语音尚未准备好", Toast.LENGTH_SHORT); return; }
        if (isPlaying) pausePlayback();
        else {
            isPlaying = true;
            playGeneration++;
            updateUi();
            speakCurrent(true);
        }
    }

    private void pausePlayback() {
        isPlaying = false;
        resetForwardStepClock();
        playGeneration++;
        handler.removeCallbacksAndMessages(null);
        if (tts != null) tts.stop();
        activeTtsUtteranceId = null;
        abandonSpeechAudioFocus();
        if (playPauseButton != null) updateUi();
    }

    private void moveNext(boolean speak) {
        if (previewAnimationRunning) return;
        releaseHeldPreviewResult();
        if (sequence.isEmpty()) return;
        boolean wasPlaying = isPlaying;
        playGeneration++;
        handler.removeCallbacksAndMessages(null);
        if (tts != null) tts.stop();
        if (currentIndex < sequence.size() - 1) {
            recordForwardStepTiming();
            currentIndex++;
            saveProgress();
            pendingStepAnimation = 1;
            updateUi();
            if (speak) speakCurrent(wasPlaying);
            else abandonSpeechAudioFocus();
        } else {
            isPlaying = false;
            abandonSpeechAudioFocus();
            updateUi();
            toast("已经到最后一个钉号", Toast.LENGTH_SHORT);
        }
    }

    private void moveNextFromPhoneTap() {
        int previousIndex = currentIndex;
        moveNext(true);
        if (currentIndex != previousIndex) sendWearablePhoneNextFeedback();
    }

    private void movePrevious(boolean speak) {
        if (previewAnimationRunning) return;
        releaseHeldPreviewResult();
        if (sequence.isEmpty()) return;
        boolean wasPlaying = isPlaying;
        playGeneration++;
        handler.removeCallbacksAndMessages(null);
        if (tts != null) tts.stop();
        if (currentIndex > 0) {
            resetForwardStepClock();
            currentIndex--;
            saveProgress();
            pendingStepAnimation = -1;
            updateUi();
            if (speak) speakCurrent(wasPlaying);
            else abandonSpeechAudioFocus();
        } else {
            abandonSpeechAudioFocus();
            toast("已经在第一个钉号", Toast.LENGTH_SHORT);
        }
    }

    private void replayCurrent() {
        if (previewAnimationRunning) return;
        releaseHeldPreviewResult();
        if (sequence.isEmpty()) { openImportPicker(); return; }
        playGeneration++;
        handler.removeCallbacksAndMessages(null);
        if (tts != null) tts.stop();
        speakCurrent(isPlaying);
    }

    private void speakCurrent(final boolean autoAdvanceAfterSpeech) {
        if (sequence.isEmpty() || !ttsReady || tts == null) return;
        final int requestedGeneration = playGeneration;
        final int requestedIndex = currentIndex;
        int nail = sequence.get(requestedIndex);
        Locale speechLocale = selectedSpeechLocale();
        tts.setLanguage(speechLocale);
        String number;
        if ("en".equals(speechLocale.getLanguage())) number = englishNumber(nail);
        else if ("zh".equals(speechLocale.getLanguage()))
            number = NailNumberFormatter.chineseNumber(nail);
        else number = String.valueOf(nail);
        String spoken = repeatTwice ? (number + ", " + number) : number;
        final String finalSpoken = spoken;
        if (activityDestroyed || requestedGeneration != playGeneration
                || requestedIndex != currentIndex) return;
        requestSpeechAudioFocus();
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,
                Math.max(0f, Math.min(1f, ttsVolumePercent / 100f)));
        String utteranceId = "nail_" + requestedGeneration + "_" + requestedIndex;
        activeTtsUtteranceId = utteranceId;
        vibratePhoneForNail();
        int result = tts.speak(finalSpoken, TextToSpeech.QUEUE_FLUSH,
                params, utteranceId);
        if (result == TextToSpeech.ERROR) {
            activeTtsUtteranceId = null;
            abandonSpeechAudioFocus();
            pausePlayback();
            toast("语音播报失败", Toast.LENGTH_LONG);
        }
    }

    private void requestSpeechAudioFocus() {
        abandonSpeechAudioFocus();
        if (!mediaDuckingEnabled || audioManager == null
                || !audioManager.isMusicActive()) return;
        if (speechFocusRequest == null) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            speechFocusRequest = new AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override public void onAudioFocusChange(int focusChange) { }
                    })
                    .build();
        }
        speechFocusHeld = audioManager.requestAudioFocus(speechFocusRequest)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void vibratePhoneForNail() {
        if (!phoneVibrationEnabled) return;
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(34, 90));
        else vibrator.vibrate(34);
    }

    private boolean performMappedAction(String action) {
        if ("previous".equals(action)) movePrevious(true);
        else if ("next".equals(action)) moveNext(true);
        else if ("toggle".equals(action)) togglePlayback();
        else if ("replay".equals(action)) replayCurrent();
        else if ("faster".equals(action)) adjustSpeechDelay(-QUICK_DELAY_STEP_MS, true);
        else if ("slower".equals(action)) adjustSpeechDelay(QUICK_DELAY_STEP_MS, true);
        else return false;
        return true;
    }

    private void abandonSpeechAudioFocus() {
        if (audioManager != null && speechFocusHeld && speechFocusRequest != null)
            audioManager.abandonAudioFocusRequest(speechFocusRequest);
        speechFocusHeld = false;
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS || tts == null) {
            ttsReady = false;
            toast("系统文字转语音初始化失败", Toast.LENGTH_LONG);
            return;
        }
        Locale speechLocale = selectedSpeechLocale();
        int languageResult = tts.setLanguage(speechLocale);
        if (languageResult == TextToSpeech.LANG_MISSING_DATA
                || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.getDefault());
            toast("未找到所选语言的语音，已使用系统默认语音", Toast.LENGTH_LONG);
        }
        tts.setSpeechRate(speechRate);
        tts.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
        ttsReady = true;
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) { }
            @Override public void onDone(String utteranceId) {
                handler.post(new Runnable() {
                    @Override public void run() {
                        if (utteranceId != null
                                && utteranceId.equals(activeTtsUtteranceId)) {
                            activeTtsUtteranceId = null;
                            abandonSpeechAudioFocus();
                        }
                    }
                });
                String[] parts = utteranceId == null ? new String[0] : utteranceId.split("_");
                if (parts.length != 3) return;
                final int generation;
                final int spokenIndex;
                try {
                    generation = Integer.parseInt(parts[1]);
                    spokenIndex = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) { return; }
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
                        if (!isPlaying || generation != playGeneration || spokenIndex != currentIndex) return;
                        if (currentIndex >= sequence.size() - 1) {
                            isPlaying = false;
                            updateUi();
                            toast("序列播报完成", Toast.LENGTH_LONG);
                            return;
                        }
                        recordForwardStepTiming();
                        currentIndex++;
                        saveProgress();
                        pendingStepAnimation = 1;
                        updateUi();
                        speakCurrent(true);
                    }
                }, delayMs);
            }
            @Override public void onError(String utteranceId) {
                handler.post(new Runnable() {
                    @Override public void run() {
                        if (utteranceId != null
                                && utteranceId.equals(activeTtsUtteranceId)) {
                            activeTtsUtteranceId = null;
                            abandonSpeechAudioFocus();
                        }
                        pausePlayback();
                        toast("语音播报失败", Toast.LENGTH_LONG);
                    }
                });
            }
        });
    }

    private Locale selectedSpeechLocale() {
        String language = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_LANGUAGE, "system");
        if ("en".equals(language)) return Locale.US;
        if ("zh".equals(language)) return Locale.SIMPLIFIED_CHINESE;
        return getResources().getConfiguration().getLocales().get(0);
    }

    private String englishNumber(int value) {
        if (value == 0) return "zero";
        if (value < 0) return "minus " + englishNumber(-value);
        StringBuilder result = new StringBuilder();
        if (value >= 1000) {
            result.append(englishNumber(value / 1000)).append(" thousand");
            value %= 1000;
            if (value > 0) result.append(' ');
        }
        if (value >= 100) {
            result.append(englishNumber(value / 100)).append(" hundred");
            value %= 100;
            if (value > 0) result.append(' ');
        }
        if (value >= 20) {
            String[] tens = {"", "", "twenty", "thirty", "forty", "fifty",
                    "sixty", "seventy", "eighty", "ninety"};
            result.append(tens[value / 10]);
            value %= 10;
            if (value > 0) result.append(' ');
        }
        if (value > 0) {
            String[] ones = {"", "one", "two", "three", "four", "five", "six",
                    "seven", "eight", "nine", "ten", "eleven", "twelve",
                    "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
                    "eighteen", "nineteen"};
            result.append(ones[value]);
        }
        return result.toString();
    }

    private void showGeneratorEntry() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(8), dp(22), dp(4));
        TextView intro = dialogLabel(isEnglish()
                ? "Choose a photo and the app will generate a monochrome circular string-art sequence entirely on this device, then open it directly in the reader.\n\nThe crop view shows the exact circular generation area. The default fit keeps the whole image inside the circle; areas outside the image are treated as white. Drag to frame or pinch to zoom within the limits. Nail 0 is at the rightmost point and numbering increases clockwise."
                : "选择一张照片后，应用会完全在本机生成单色圆形绕线序列，并直接打开播报器。\n\n裁切页显示最终参与计算的圆形区域。默认会让整张图片完整进入圆内，图片以外的区域按白色处理；可拖动取景、双指限位缩放。钉号固定为 0 号正右、顺时针递增。");
        intro.setLineSpacing(dp(3), 1f);
        panel.addView(intro);

        new AlertDialog.Builder(this)
                .setTitle(tr("图片生成绕线画"))
                .setView(panel)
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("选择图片"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { openImagePicker(); }
                }).show();
    }

    private Bitmap decodeGeneratorBitmap(Uri uri) throws IOException {
        InputStream probe = getContentResolver().openInputStream(uri);
        if (probe == null) throw new IOException("无法打开图片");
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try { BitmapFactory.decodeStream(probe, null, bounds); } finally { probe.close(); }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IOException("不是可识别的图片");
        int largest = Math.max(bounds.outWidth, bounds.outHeight);
        int sample = 1;
        while (largest / sample > 1600) sample *= 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        InputStream input = getContentResolver().openInputStream(uri);
        if (input == null) throw new IOException("无法读取图片");
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            if (bitmap == null) throw new IOException("图片解码失败");
            Bitmap monochrome = makeMonochromeBitmap(bitmap);
            if (monochrome != bitmap) bitmap.recycle();
            return monochrome;
        } finally { input.close(); }
    }

    /**
     * The generator only uses luminance, so show and retain that same monochrome
     * source from the crop step onward. Drawing onto white first also gives
     * transparent PNG pixels the same physical-board treatment as generation.
     */
    private Bitmap makeMonochromeBitmap(Bitmap source) throws IOException {
        try {
            Bitmap result = Bitmap.createBitmap(
                    source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawColor(Color.WHITE);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0f);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
            canvas.drawBitmap(source, 0f, 0f, paint);
            return result;
        } catch (OutOfMemoryError error) {
            throw new IOException("图片过大，无法转换为黑白图", error);
        }
    }

    private String stripFileExtension(String value) {
        int dot = value == null ? -1 : value.lastIndexOf('.');
        return dot > 0 ? value.substring(0, dot) : (value == null ? "图片" : value);
    }

    private String sequenceSourceBaseName(String value) {
        String base = stripFileExtension(value == null ? "绕线画" : value).trim();
        base = TXT_EXPORT_SUFFIX_PATTERN.matcher(base).replaceFirst("").trim();
        base = SEQUENCE_NAME_SUFFIX_PATTERN.matcher(base).replaceFirst("").trim();
        base = GENERATED_LABEL_PREFIX_PATTERN.matcher(base).replaceFirst("").trim();
        base = GENERATED_LABEL_SUFFIX_PATTERN.matcher(base).replaceFirst("").trim();
        while (base.endsWith("·") || base.endsWith("_") || base.endsWith("-"))
            base = base.substring(0, base.length() - 1).trim();
        return base.length() == 0 ? "绕线画" : base;
    }

    private byte[] createCropThumbnail(Bitmap source, float cropX, float cropY,
                                       float cropZoom) {
        if (source == null || source.isRecycled()) return null;
        Bitmap thumbnail = null;
        try {
            thumbnail = Bitmap.createBitmap(
                    SAVE_THUMBNAIL_SIZE, SAVE_THUMBNAIL_SIZE, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(thumbnail);
            canvas.drawColor(Color.WHITE);
            float cropSize = StringArtGenerator.cropSizePx(source, cropZoom);
            float x0 = cropX * source.getWidth() - cropSize * .5f;
            float y0 = cropY * source.getHeight() - cropSize * .5f;
            float scale = SAVE_THUMBNAIL_SIZE / cropSize;
            Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            int saved = canvas.save();
            Path circle = new Path();
            circle.addCircle(SAVE_THUMBNAIL_SIZE * .5f, SAVE_THUMBNAIL_SIZE * .5f,
                    SAVE_THUMBNAIL_SIZE * .5f - 1f, Path.Direction.CW);
            canvas.clipPath(circle);
            canvas.scale(scale, scale);
            canvas.translate(-x0, -y0);
            ColorMatrix grayscale = new ColorMatrix();
            grayscale.setSaturation(0f);
            imagePaint.setColorFilter(new ColorMatrixColorFilter(grayscale));
            canvas.drawBitmap(source, 0f, 0f, imagePaint);
            canvas.restoreToCount(saved);
            Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
            outline.setStyle(Paint.Style.STROKE);
            outline.setStrokeWidth(2f);
            outline.setColor(Color.rgb(205, 205, 212));
            canvas.drawCircle(SAVE_THUMBNAIL_SIZE * .5f, SAVE_THUMBNAIL_SIZE * .5f,
                    SAVE_THUMBNAIL_SIZE * .5f - 2f, outline);
            ByteArrayOutputStream output = new ByteArrayOutputStream(20 * 1024);
            if (!thumbnail.compress(Bitmap.CompressFormat.PNG, 100, output)) return null;
            byte[] result = output.toByteArray();
            return result.length <= MAX_SAVE_THUMBNAIL_BYTES ? result : null;
        } catch (RuntimeException ignored) {
            return null;
        } finally {
            if (thumbnail != null && !thumbnail.isRecycled()) thumbnail.recycle();
        }
    }

    private byte[] createFinalThumbnail(ArrayList<Integer> values, int nails,
                                        float lineMm, int circleMm) {
        if (values == null || values.size() < 2 || nails < 2) return null;
        Bitmap thumbnail = null;
        try {
            thumbnail = Bitmap.createBitmap(
                    SAVE_THUMBNAIL_SIZE, SAVE_THUMBNAIL_SIZE, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(thumbnail);
            canvas.drawColor(Color.rgb(248, 247, 251));
            float center = SAVE_THUMBNAIL_SIZE * .5f;
            float radius = SAVE_THUMBNAIL_SIZE * .46f;
            float strokeRatio = Math.max(MIN_THREAD_MM,
                    Math.min(MAX_THREAD_MM, lineMm)) / Math.max(1f, circleMm);
            Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
            line.setStyle(Paint.Style.STROKE);
            line.setStrokeWidth(Math.max(.12f, 2f * radius * strokeRatio));
            int alpha = Math.max(26, Math.min(82,
                    Math.round(26f + SAVE_THUMBNAIL_SIZE * strokeRatio * 90f)));
            line.setColor(Color.argb(alpha, 18, 18, 18));
            for (int i = 1; i < values.size(); i++) {
                int from = values.get(i - 1);
                int to = values.get(i);
                if (from < 0 || from >= nails || to < 0 || to >= nails) continue;
                double fromAngle = Math.PI * 2d * from / nails;
                double toAngle = Math.PI * 2d * to / nails;
                canvas.drawLine(
                        center + (float) Math.cos(fromAngle) * radius,
                        center + (float) Math.sin(fromAngle) * radius,
                        center + (float) Math.cos(toAngle) * radius,
                        center + (float) Math.sin(toAngle) * radius,
                        line);
            }
            Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
            rim.setStyle(Paint.Style.STROKE);
            rim.setStrokeWidth(1.5f);
            rim.setColor(Color.rgb(75, 75, 82));
            canvas.drawCircle(center, center, radius, rim);
            ByteArrayOutputStream output = new ByteArrayOutputStream(24 * 1024);
            if (!thumbnail.compress(Bitmap.CompressFormat.PNG, 100, output)) return null;
            byte[] result = output.toByteArray();
            return result.length <= MAX_SAVE_THUMBNAIL_BYTES ? result : null;
        } catch (RuntimeException ignored) {
            return null;
        } finally {
            if (thumbnail != null && !thumbnail.isRecycled()) thumbnail.recycle();
        }
    }

    /**
     * SAR4 thumbnails are deliberately monochrome.  Older builds trusted the crop source to
     * have already been desaturated, which allowed a coloured source/cache to be written into
     * the save unchanged.  Normalise those existing PNGs once when the save list is read.
     */
    private byte[] normalizeThumbnailToMonochrome(byte[] encoded) {
        if (encoded == null || encoded.length == 0) return encoded;
        Bitmap source = null;
        Bitmap monochrome = null;
        try {
            source = BitmapFactory.decodeByteArray(encoded, 0, encoded.length);
            if (source == null) return encoded;
            boolean hasColour = false;
            int width = source.getWidth();
            int height = source.getHeight();
            int sampleStep = Math.max(1, Math.min(width, height) / 48);
            for (int y = 0; y < height && !hasColour; y += sampleStep) {
                for (int x = 0; x < width; x += sampleStep) {
                    int pixel = source.getPixel(x, y);
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);
                    if (Math.max(red, Math.max(green, blue))
                            - Math.min(red, Math.min(green, blue)) > 3) {
                        hasColour = true;
                        break;
                    }
                }
            }
            if (!hasColour) return encoded;
            monochrome = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(monochrome);
            canvas.drawColor(Color.WHITE);
            ColorMatrix grayscale = new ColorMatrix();
            grayscale.setSaturation(0f);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            paint.setColorFilter(new ColorMatrixColorFilter(grayscale));
            canvas.drawBitmap(source, 0f, 0f, paint);
            ByteArrayOutputStream output = new ByteArrayOutputStream(encoded.length);
            if (!monochrome.compress(Bitmap.CompressFormat.PNG, 100, output)) return encoded;
            byte[] result = output.toByteArray();
            return result.length <= MAX_SAVE_THUMBNAIL_BYTES ? result : encoded;
        } catch (RuntimeException ignored) {
            return encoded;
        } finally {
            if (source != null && !source.isRecycled()) source.recycle();
            if (monochrome != null && !monochrome.isRecycled()) monochrome.recycle();
        }
    }

    private byte[] copyBytes(byte[] value) {
        return value == null ? null : value.clone();
    }

    private void restoreCurrentThumbnailFromActiveProject() {
        if (sequence.isEmpty() || activeAutoProjectFile == null) return;
        try {
            currentProjectThumbnail = copyBytes(
                    readSave(activeAutoProjectFile, false).thumbnailBytes);
        } catch (IOException ignored) { }
    }

    private void showCropFullscreen() {
        if (pendingGeneratorBitmap == null) return;
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8), dp(34), dp(8), dp(8));
        panel.setBackgroundColor(Color.BLACK);
        final SquareCropView crop = new SquareCropView(this);
        crop.setBitmap(pendingGeneratorBitmap);
        crop.setHintText(tr("黑白预览 · 拖动取景 · 双指缩放"));
        crop.setCrop(generatorCropX, generatorCropY, generatorCropZoom);
        panel.addView(crop, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        TextView tip = new TextView(this);
        tip.setText(isEnglish()
                ? "The circle is the final input · whole-image fit to 4× zoom"
                : "圆框内为最终计算区域 · 整图适配至 4× 限位缩放");
        tip.setTextColor(MUTED); tip.setTextSize(14f); tip.setGravity(Gravity.CENTER);
        panel.addView(tip, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(32)));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL); actions.setGravity(Gravity.CENTER); actions.setPadding(0, dp(6), 0, 0);
        Button cancel = makeButton("取消");
        cancel.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { dialog.dismiss(); } });
        actions.addView(cancel, weightedButtonParams());
        Button next = makeButton("下一步"); styleAccentButton(next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                generatorCropX = crop.getCenterX(); generatorCropY = crop.getCenterY(); generatorCropZoom = crop.getZoom();
                dialog.dismiss(); showGeneratorConfig();
            }
        });
        actions.addView(next, weightedButtonParams());
        panel.addView(actions, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(62)));
        dialog.setContentView(panel);
        dialog.show();
    }

    private void showGeneratorConfig() {
        if (pendingGeneratorBitmap == null) return;
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(8), dp(22), 0);
        TextView note = dialogLabel(isEnglish()
                ? "“" + pendingGeneratorName + "”\nThe monochrome crop is saved. The area inside the circle will be used for generation.\nNail 0 is at the rightmost point; numbering increases clockwise."
                : "“" + pendingGeneratorName + "”\n黑白裁切已保存；圆框内就是最终参与计算的区域。\n0 号正右，编号顺时针递增。");
        note.setTextSize(14f);
        note.setLineSpacing(dp(3), 1f);
        panel.addView(note);

        Button cropButton = makeButton("重新裁切图片");
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (generatorConfigDialog != null) generatorConfigDialog.dismiss();
                showCropFullscreen();
            }
        });
        LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        cropParams.setMargins(0, dp(10), 0, dp(4));
        panel.addView(cropButton, cropParams);

        final TextView nailsLabel = dialogLabel("钉子数　当前：" + generatorNails);
        nailsLabel.setPadding(0, dp(14), 0, 0);
        panel.addView(nailsLabel);
        final SeekBar nails = new SeekBar(this);
        nails.setMax(40); // 100 to 500, every 10
        nails.setProgress(Math.max(0, Math.min(40, (generatorNails - 100) / 10)));
        panel.addView(nails);
        nails.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nailsLabel.setText(tr("钉子数　当前：" + (100 + progress * 10)));
            }
        });

        final TextView stepsLabel = dialogLabel("绕线步数　当前：" + generatorSteps);
        stepsLabel.setPadding(0, dp(12), 0, 0);
        panel.addView(stepsLabel);
        final SeekBar steps = new SeekBar(this);
        steps.setMax(38); // 1000 to 20000, every 500
        steps.setProgress(Math.max(0, Math.min(38, (generatorSteps - 1000) / 500)));
        panel.addView(steps);
        steps.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stepsLabel.setText(tr("绕线步数　当前：" + (1000 + progress * 500)));
            }
        });
        final EditText circleInput = numericInput(String.valueOf(generatorCircleMm), "80～1200 mm");
        final EditText lineInput = decimalInput(String.format(Locale.CHINA, "%.2f", generatorLineMm), "0.01～1.00 mm");
        final ScrollView scroll = new ScrollView(this);
        TextView circleTitle = dialogLabel(isEnglish()
                ? "Nail circle diameter (final value)"
                : "钉位圆直径（最终使用）");
        circleTitle.setPadding(0, dp(12), 0, 0); panel.addView(circleTitle); panel.addView(circleInput);
        final TextView circleHint = dialogLabel(isEnglish()
                ? "The diameter should be at least 20 mm smaller than the board's shortest side, leaving 10 mm on every edge."
                : "直径应比板材最短边至少小 20 mm，确保四周各留 10 mm。");
        circleHint.setTextSize(12f);
        circleHint.setTextColor(MUTED);
        circleHint.setPadding(0, dp(2), 0, 0);
        panel.addView(circleHint);
        TextView lineTitle = dialogLabel(isEnglish()
                ? "Thread diameter / thickness (affects simulation)"
                : "线的直径 / 粗细（影响算法模拟）");
        lineTitle.setPadding(0, dp(8), 0, 0); panel.addView(lineTitle); panel.addView(lineInput);
        final CheckBox autoStopBox = new CheckBox(this);
        autoStopBox.setText(tr("自动防全黑（达到安全墨量后提前停止）"));
        autoStopBox.setTextColor(FG);
        autoStopBox.setChecked(generatorAutoStop);
        panel.addView(autoStopBox);
        final TextView limits = dialogLabel(isEnglish()
                ? "Ranges: circle 80–1200 mm; thread 0.01–1.00 mm.\nOut-of-range values snap to the nearest limit when editing ends."
                : "范围：圆 80～1200 mm；线 0.01～1.00 mm。\n结束输入时，超出范围的数值会自动移到最近限位。");
        limits.setTextSize(12f); limits.setPadding(0, dp(4), 0, 0); panel.addView(limits);
        circleInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(final View v, boolean focused) {
                if (focused) {
                    scroll.postDelayed(new Runnable() { @Override public void run() { scroll.smoothScrollTo(0, Math.max(0, v.getTop() - dp(72))); } }, 180);
                } else {
                    enforceCircleDiameterInput(circleInput);
                }
            }
        });
        lineInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(final View v, boolean focused) {
                if (focused) {
                    scroll.postDelayed(new Runnable() {
                        @Override public void run() {
                            scroll.smoothScrollTo(0, Math.max(0, v.getTop() - dp(72)));
                        }
                    }, 180);
                } else {
                    enforceLineDiameterInput(lineInput);
                }
            }
        });
        circleInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lineInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        circleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    enforceCircleDiameterInput(circleInput);
                }
                return false;
            }
        });
        lineInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    enforceLineDiameterInput(lineInput);
                }
                return false;
            }
        });

        scroll.addView(panel);
        final AlertDialog configDialog = new AlertDialog.Builder(this)
                .setTitle(tr("图片 → 绕线序列")).setView(scroll)
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("开始生成"), null)
                .create();
        generatorConfigDialog = configDialog;
        configDialog.show();
        configDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                enforceCircleDiameterInput(circleInput);
                enforceLineDiameterInput(lineInput);
                int circle = parseIntOr(circleInput, -1);
                float line = parseFloatOr(lineInput, -1f);
                if (circle < MIN_CIRCLE_MM || circle > MAX_CIRCLE_MM
                        || line < MIN_THREAD_MM || line > MAX_THREAD_MM) {
                    showError(isEnglish()
                            ? "Values are out of range. Circle: 80–1200 mm; thread: 0.01–1.00 mm."
                            : "参数超出范围。圆：80～1200 mm；线：0.01～1.00 mm。");
                    return;
                }
                configDialog.dismiss();
                startLocalGeneration(100 + nails.getProgress() * 10,
                        1000 + steps.getProgress() * 500, circle, line,
                        autoStopBox.isChecked(), generatorCropX, generatorCropY, generatorCropZoom,
                        true);
            }
        });
    }

    private EditText numericInput(String value, String hint) {
        EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER); input.setText(value); input.setHint(hint); input.setSingleLine(true); return input;
    }
    private EditText decimalInput(String value, String hint) {
        EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); input.setText(value); input.setHint(hint); input.setSingleLine(true); return input;
    }
    private int parseIntOr(EditText input, int fallback) { try { return Integer.parseInt(input.getText().toString().trim()); } catch (Exception e) { return fallback; } }
    private float parseFloatOr(EditText input, float fallback) { try { return Float.parseFloat(input.getText().toString().trim()); } catch (Exception e) { return fallback; } }

    private void enforceLineDiameterInput(EditText input) {
        float value = parseFloatOr(input, generatorLineMm);
        value = Math.max(MIN_THREAD_MM, Math.min(MAX_THREAD_MM, value));
        value = Math.round(value * 100f) / 100f;
        String formatted = String.format(Locale.US, "%.2f", value);
        if (!formatted.equals(input.getText().toString().trim())) {
            input.setText(formatted);
            input.setSelection(formatted.length());
        }
    }

    private void enforceCircleDiameterInput(EditText circleInput) {
        String circleRaw = circleInput.getText().toString().trim();
        if (circleRaw.length() == 0) return;
        int circle = parseIntOr(circleInput, MIN_CIRCLE_MM);
        circle = Math.max(MIN_CIRCLE_MM, Math.min(MAX_CIRCLE_MM, circle));
        String circleText = String.valueOf(circle);
        if (!circleText.equals(circleRaw)) { circleInput.setText(circleText); circleInput.setSelection(circleText.length()); }
    }

    private void startLocalGeneration(final int nails, final int steps, final int circleMm,
                                      final float lineMm, final boolean autoStop, final float cropX, final float cropY,
                                      final float cropZoom, final boolean syncPreviewLineWidth) {
        final Bitmap source = pendingGeneratorBitmap;
        if (source == null) return;
        if (generatedPreviewDialog != null && generatedPreviewDialog.isShowing())
            generatedPreviewDialog.dismiss();
        if (generatorAnimationRunnable != null) handler.removeCallbacks(generatorAnimationRunnable);
        generatorCancelled.set(false);
        final LinearLayout progressPanel = new LinearLayout(this);
        progressPanel.setOrientation(LinearLayout.VERTICAL);
        progressPanel.setPadding(dp(18), dp(8), dp(18), dp(4));
        final ScrollView progressScroll = new ScrollView(this);
        progressScroll.setFillViewport(false);
        progressScroll.setVerticalScrollBarEnabled(true);
        progressScroll.addView(progressPanel, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        final FrameLayout progressFrame = new FrameLayout(this);
        progressFrame.addView(progressScroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        SideScrollBar progressSideScrollBar = new SideScrollBar(this, progressScroll);
        progressSideScrollBar.setContentDescription(
                isEnglish() ? "Scroll page" : "滚动页面");
        progressFrame.addView(progressSideScrollBar, new FrameLayout.LayoutParams(
                dp(24), FrameLayout.LayoutParams.MATCH_PARENT, Gravity.END));
        final GenerationProgressView drawing = new GenerationProgressView(
                this, nails, circleMm, lineMm);
        progressPanel.addView(drawing, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(270)));
        final TextView status = dialogLabel(isEnglish() ? "Preparing image…" : "正在准备图片…");
        status.setGravity(Gravity.CENTER);
        status.setTextSize(13f);
        status.setPadding(dp(4), dp(10), dp(4), dp(4));
        progressPanel.addView(status);
        final TextView speedLabel = dialogLabel(isEnglish()
                ? "Drawing animation speed: " + generatorVisualSpeed + "×"
                : "绘制动画速度：" + generatorVisualSpeed + "×");
        speedLabel.setTextSize(13f);
        progressPanel.addView(speedLabel);
        final SeekBar speedBar = new SeekBar(this);
        speedBar.setMax(19);
        speedBar.setProgress(generatorVisualSpeed - 1);
        progressPanel.addView(speedBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(38)));
        final int[] visualSpeed = new int[] { generatorVisualSpeed };
        speedBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                visualSpeed[0] = progress + 1;
                generatorVisualSpeed = visualSpeed[0];
                speedLabel.setText(isEnglish()
                        ? "Drawing animation speed: " + visualSpeed[0] + "×"
                        : "绘制动画速度：" + visualSpeed[0] + "×");
                if (fromUser) prefs.edit().putInt(KEY_GENERATOR_VISUAL_SPEED,
                        visualSpeed[0]).apply();
            }
        });
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("正在本地生成"))
                .setView(progressFrame)
                .setNegativeButton(tr("取消"), null)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override public void onCancel(DialogInterface unused) {
                generatorCancelled.set(true);
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface unused) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { generatorCancelled.set(true); }
                });
            }
        });
        generatorProgressDialog = dialog;
        dialog.show();
        generatorExecutor.execute(new Runnable() {
            @Override public void run() {
                try {
                    final ArrayList<Integer> result = StringArtGenerator.generate(source, nails, steps,
                            circleMm, lineMm, autoStop, cropX, cropY, cropZoom,
                            generatorCancelled, new StringArtGenerator.ProgressListener() {
                                @Override public void onProgress(final int complete, final int total) {
                                    if (complete == total || complete % 40 == 0) handler.post(new Runnable() {
                                        @Override public void run() {
                                            if (activityDestroyed) return;
                                            if (generatorProgressDialog != null && generatorProgressDialog.isShowing())
                                                status.setText(isEnglish()
                                                        ? "Calculating string " + complete + " / " + total + "…"
                                                        : "正在计算第 " + complete + " / " + total + " 根线…");
                                        }
                                    });
                                }
                            });
                    handler.post(new Runnable() {
                        @Override public void run() {
                            if (activityDestroyed) return;
                            if (generatorCancelled.get() || result == null || result.size() < 2) {
                                if (generatorProgressDialog != null) generatorProgressDialog.dismiss();
                                toast("已取消生成", Toast.LENGTH_SHORT);
                                return;
                            }
                            status.setText(isEnglish()
                                    ? "Calculation complete · drawing 0 / " + (result.size() - 1)
                                    : "计算完成 · 正在绘制 0 / " + (result.size() - 1));
                            drawing.appendUntil(result, 1);
                            final int[] revealed = new int[] { 1 };
                            generatorAnimationRunnable = new Runnable() {
                                @Override public void run() {
                                    if (activityDestroyed) return;
                                    if (generatorCancelled.get()) {
                                        if (generatorProgressDialog != null)
                                            generatorProgressDialog.dismiss();
                                        toast("已取消生成", Toast.LENGTH_SHORT);
                                        return;
                                    }
                                    int batch = Math.max(1, visualSpeed[0] * 4);
                                    revealed[0] = Math.min(result.size(), revealed[0] + batch);
                                    drawing.appendUntil(result, revealed[0]);
                                    status.setText(isEnglish()
                                            ? "Calculation complete · drawing "
                                                + (revealed[0] - 1) + " / " + (result.size() - 1)
                                            : "计算完成 · 正在绘制 "
                                                + (revealed[0] - 1) + " / " + (result.size() - 1));
                                    if (revealed[0] < result.size()) {
                                        handler.postDelayed(this, 32L);
                                        return;
                                    }
                                    generatedCandidate = result;
                                    pendingGeneratedThumbnail = createCropThumbnail(
                                            source, cropX, cropY, cropZoom);
                                    generatorNails = nails;
                                    generatorSteps = steps;
                                    generatorCircleMm = circleMm;
                                    generatorLineMm = lineMm;
                                    // A newly submitted generator configuration aligns the
                                    // preview slider with its physical thread diameter. A
                                    // parameter-only regeneration from the preview preserves
                                    // the user's separate preview-width override.
                                    if (syncPreviewLineWidth) previewCustomLineMm = lineMm;
                                    generatorAutoStop = autoStop;
                                    prefs.edit()
                                            .putFloat(KEY_PREVIEW_CUSTOM_LINE_MM, previewCustomLineMm)
                                            .putBoolean(KEY_PREVIEW_USE_ACTUAL_RATIO, previewUseActualRatio)
                                            .putBoolean(KEY_GENERATOR_AUTO_STOP, autoStop)
                                            .putInt(KEY_GENERATOR_VISUAL_SPEED, visualSpeed[0]).apply();
                                    if (generatorProgressDialog != null)
                                        generatorProgressDialog.dismiss();
                                    generatorAnimationRunnable = null;
                                    showGeneratedPreview();
                                }
                            };
                            handler.post(generatorAnimationRunnable);
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override public void run() {
                            if (activityDestroyed) return;
                            if (generatorProgressDialog != null) generatorProgressDialog.dismiss();
                            showError("本地生成失败：" + safeMessage(e));
                        }
                    });
                }
            }
        });
    }

    private void showGeneratedPreview() {
        if (generatedCandidate == null || generatedCandidate.size() < 2) return;
        showSequencePreview(generatedCandidate, generatorNails, null, generatorLineMm,
                generatorCircleMm);
    }

    private void showSequencePreview(final ArrayList<Integer> previewCandidate,
                                     final int previewNails,
                                     final String importName,
                                     final float previewProjectLineMm,
                                     final int previewCircleMm) {
        if (previewCandidate == null || previewCandidate.size() < 2) return;
        final boolean importingTxt = importName != null;
        final boolean[] restarting = new boolean[] { false };
        final float[] selectedProjectLineMm = new float[] { previewProjectLineMm };
        final int[] selectedCircleMm = new int[] { previewCircleMm };
        final AlertDialog[] dialogHolder = new AlertDialog[1];
        final TextView[] infoHolder = new TextView[1];
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(4), dp(14), dp(10));
        ScrollView previewScroll = new ScrollView(this);
        previewScroll.setFillViewport(false);
        previewScroll.setVerticalScrollBarEnabled(true);
        previewScroll.addView(panel, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        FrameLayout previewFrame = new FrameLayout(this);
        previewFrame.addView(previewScroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        SideScrollBar sideScrollBar = new SideScrollBar(this, previewScroll);
        sideScrollBar.setContentDescription(isEnglish() ? "Scroll page" : "滚动页面");
        FrameLayout.LayoutParams sideScrollParams = new FrameLayout.LayoutParams(
                dp(24), FrameLayout.LayoutParams.MATCH_PARENT, Gravity.END);
        previewFrame.addView(sideScrollBar, sideScrollParams);
        final GeneratedPreviewView preview = new GeneratedPreviewView(this, previewCandidate,
                previewNails, previewProjectLineMm, previewCircleMm,
                importingTxt || previewUseActualRatio, previewCustomLineMm);
        panel.addView(preview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(330)));

        final TextView lineWidthLabel = dialogLabel(importingTxt
                ? (isEnglish()
                    ? "Thread diameter  Current: " + formatLineWidthMm(previewProjectLineMm)
                    : "线径　当前：" + formatLineWidthMm(previewProjectLineMm))
                : previewLineWidthLabel(
                    previewUseActualRatio, previewCustomLineMm, previewProjectLineMm));
        lineWidthLabel.setTextSize(12f);
        lineWidthLabel.setPadding(0, dp(6), 0, 0);
        panel.addView(lineWidthLabel);
        final SeekBar lineWidthBar = new SeekBar(this);
        lineWidthBar.setMax(99); // 0.01–1.00 mm, every 0.01 mm
        float initiallyShownLineMm = importingTxt ? previewProjectLineMm : previewUseActualRatio
                ? previewProjectLineMm : previewCustomLineMm;
        lineWidthBar.setProgress(Math.max(0, Math.min(99,
                Math.round((initiallyShownLineMm - MIN_THREAD_MM) * 100f))));
        panel.addView(lineWidthBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(34)));
        lineWidthBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float selectedMm = MIN_THREAD_MM + progress / 100f;
                if (fromUser) {
                    if (importingTxt) {
                        selectedProjectLineMm[0] = selectedMm;
                        preview.setProjectLineMm(selectedMm);
                        preview.setThreadDisplay(true, selectedMm);
                        if (infoHolder[0] != null)
                            infoHolder[0].setText(importedPreviewInfoText(
                                    previewCandidate, previewNails, selectedMm,
                                    selectedCircleMm[0], importName));
                    } else {
                        previewUseActualRatio = false;
                        previewCustomLineMm = selectedMm;
                        preview.setThreadDisplay(false, selectedMm);
                        persistPreviewLineWidth();
                        updatePreviewViews();
                    }
                }
                lineWidthLabel.setText(importingTxt
                        ? (isEnglish()
                            ? "Thread diameter  Current: " + formatLineWidthMm(selectedMm)
                            : "线径　当前：" + formatLineWidthMm(selectedMm))
                        : previewLineWidthLabel(false, selectedMm));
            }
        });
        Button actualRatioButton = makeButton(isEnglish()
                ? "Use actual project thread diameter (" + formatLineWidthMm(previewProjectLineMm) + ")"
                : "回到实际比例（项目线径 " + formatLineWidthMm(previewProjectLineMm) + "）");
        actualRatioButton.setTextSize(11f);
        actualRatioButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                previewUseActualRatio = true;
                previewCustomLineMm = previewProjectLineMm;
                lineWidthBar.setProgress(Math.max(0, Math.min(99,
                        Math.round((previewProjectLineMm - MIN_THREAD_MM) * 100f))));
                lineWidthLabel.setText(previewLineWidthLabel(
                        true, previewCustomLineMm, previewProjectLineMm));
                preview.setThreadDisplay(true, previewCustomLineMm);
                persistPreviewLineWidth();
                updatePreviewViews();
            }
        });
        if (!importingTxt) panel.addView(actualRatioButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(40)));

        TextView parameterTitle = dialogLabel(isEnglish()
                ? "Generation parameters"
                : "生成参数");
        parameterTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        parameterTitle.setTextSize(15f);
        parameterTitle.setPadding(0, dp(12), 0, 0);
        if (!importingTxt) panel.addView(parameterTitle);
        TextView parameterHint = dialogLabel(isEnglish()
                ? "Changing nails, maximum strings or auto-stop regenerates the result. Circle diameter, preview thread diameter and animation speed are display/physical parameters and do not regenerate it."
                : "修改钉数、最大线数或自动停止会重新生成；圆径、预览线径和动画速度属于显示 / 物理参数，不会重算序列。");
        parameterHint.setTextSize(11f);
        parameterHint.setTextColor(MUTED);
        parameterHint.setPadding(0, dp(2), 0, dp(2));
        if (!importingTxt) panel.addView(parameterHint);

        Button recropButton = makeButton("重新裁切图片");
        recropButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                restarting[0] = true;
                if (dialogHolder[0] != null) dialogHolder[0].dismiss();
                showCropFullscreen();
            }
        });
        LinearLayout.LayoutParams recropParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
        recropParams.setMargins(0, dp(4), 0, dp(6));
        if (!importingTxt) panel.addView(recropButton, recropParams);

        final TextView nailsLabel = dialogLabel(isEnglish()
                ? "Nails  Current: " + generatorNails
                : "钉子数　当前：" + generatorNails);
        nailsLabel.setTextSize(13f);
        if (!importingTxt) panel.addView(nailsLabel);
        final SeekBar nailsBar = new SeekBar(this);
        nailsBar.setMax(40);
        nailsBar.setProgress(Math.max(0, Math.min(40, (generatorNails - 100) / 10)));
        if (!importingTxt) panel.addView(nailsBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(34)));

        final TextView stepsLabel = dialogLabel(isEnglish()
                ? "Maximum strings  Current: " + generatorSteps
                : "最大绕线数　当前：" + generatorSteps);
        stepsLabel.setTextSize(13f);
        if (!importingTxt) panel.addView(stepsLabel);
        final SeekBar stepsBar = new SeekBar(this);
        stepsBar.setMax(38);
        stepsBar.setProgress(Math.max(0, Math.min(38, (generatorSteps - 1000) / 500)));
        if (!importingTxt) panel.addView(stepsBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(34)));

        final TextView circleLabel = dialogLabel(isEnglish()
                ? "Nail circle diameter  Current: " + previewCircleMm
                    + " mm"
                : "钉位圆直径　当前：" + previewCircleMm
                    + " mm");
        circleLabel.setTextSize(13f);
        panel.addView(circleLabel);
        final int circleStepMm = 5;
        final int circleStepCount = (MAX_CIRCLE_MM - MIN_CIRCLE_MM) / circleStepMm;
        final SeekBar circleBar = new SeekBar(this);
        circleBar.setMax(circleStepCount);
        circleBar.setProgress(Math.max(0, Math.min(circleStepCount,
                Math.round((previewCircleMm - MIN_CIRCLE_MM) / (float) circleStepMm))));
        panel.addView(circleBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(34)));
        circleBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress,
                                                    boolean fromUser) {
                int selected = Math.min(MAX_CIRCLE_MM,
                        MIN_CIRCLE_MM + progress * circleStepMm);
                circleLabel.setText(isEnglish()
                        ? "Nail circle diameter  Current: " + selected
                            + " mm"
                        : "钉位圆直径　当前：" + selected
                            + " mm");
                if (!fromUser) return;
                preview.setCircleDiameterMm(selected);
                if (importingTxt) {
                    selectedCircleMm[0] = selected;
                    if (infoHolder[0] != null)
                        infoHolder[0].setText(importedPreviewInfoText(
                                previewCandidate, previewNails,
                                selectedProjectLineMm[0], selected, importName));
                } else {
                    generatorCircleMm = selected;
                    prefs.edit().putInt(KEY_PROJECT_CIRCLE_MM, selected).apply();
                    if (infoHolder[0] != null)
                        infoHolder[0].setText(generatedPreviewInfoText());
                    updatePreviewViews();
                }
            }
        });

        final CheckBox autoStopBox = new CheckBox(this);
        autoStopBox.setText(tr("自动防全黑（达到安全墨量后提前停止）"));
        autoStopBox.setTextColor(FG);
        autoStopBox.setChecked(generatorAutoStop);
        if (!importingTxt) panel.addView(autoStopBox);

        final TextView animationSpeedLabel = dialogLabel(isEnglish()
                ? "Drawing animation speed: " + generatorVisualSpeed + "×"
                : "绘制动画速度：" + generatorVisualSpeed + "×");
        animationSpeedLabel.setTextSize(13f);
        if (!importingTxt) panel.addView(animationSpeedLabel);
        final SeekBar animationSpeedBar = new SeekBar(this);
        animationSpeedBar.setMax(19);
        animationSpeedBar.setProgress(generatorVisualSpeed - 1);
        if (!importingTxt) panel.addView(animationSpeedBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(34)));
        animationSpeedBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                generatorVisualSpeed = progress + 1;
                animationSpeedLabel.setText(isEnglish()
                        ? "Drawing animation speed: " + generatorVisualSpeed + "×"
                        : "绘制动画速度：" + generatorVisualSpeed + "×");
                if (fromUser) prefs.edit().putInt(KEY_GENERATOR_VISUAL_SPEED,
                        generatorVisualSpeed).apply();
            }
        });

        nailsBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nailsLabel.setText(isEnglish()
                        ? "Nails  Current: " + (100 + progress * 10)
                        : "钉子数　当前：" + (100 + progress * 10));
            }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                regenerateFromPreview(dialogHolder[0], restarting, nailsBar, stepsBar,
                        autoStopBox);
            }
        });
        stepsBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stepsLabel.setText(isEnglish()
                        ? "Maximum strings  Current: " + (1000 + progress * 500)
                        : "最大绕线数　当前：" + (1000 + progress * 500));
            }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                regenerateFromPreview(dialogHolder[0], restarting, nailsBar, stepsBar,
                        autoStopBox);
            }
        });
        autoStopBox.setOnCheckedChangeListener(
                new android.widget.CompoundButton.OnCheckedChangeListener() {
                    @Override public void onCheckedChanged(android.widget.CompoundButton buttonView,
                                                           boolean isChecked) {
                        regenerateFromPreview(dialogHolder[0], restarting, nailsBar, stepsBar,
                                autoStopBox);
                    }
                });

        TextView info = dialogLabel(importingTxt
                ? importedPreviewInfoText(previewCandidate, previewNails,
                    selectedProjectLineMm[0], selectedCircleMm[0], importName)
                : generatedPreviewInfoText());
        infoHolder[0] = info;
        info.setTextSize(12f); info.setGravity(Gravity.CENTER); info.setPadding(0, dp(8), 0, dp(4)); panel.addView(info);
        Button alternativeGenerator = makeButton(isEnglish()
                ? "Not satisfied? Try stringar.com and import its TXT"
                : "效果不满意？可尝试 stringar.com，再导入其 TXT");
        alternativeGenerator.setTextSize(10f);
        alternativeGenerator.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openExternalUri("https://stringar.com/"); }
        });
        if (!importingTxt) panel.addView(alternativeGenerator, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(40)));
        TextView externalSiteNote = dialogLabel(isEnglish()
                ? "This opens an independent third-party website in your browser; its data practices are separate from this offline app."
                : "该入口会在浏览器打开独立第三方网站；其数据处理规则与本离线应用无关。");
        externalSiteNote.setTextSize(10f);
        externalSiteNote.setTextColor(MUTED);
        externalSiteNote.setGravity(Gravity.CENTER);
        externalSiteNote.setPadding(dp(4), dp(2), dp(4), dp(6));
        if (!importingTxt) panel.addView(externalSiteNote);
        Button templateButton = makeButton("生成匹配的钉位模板 PDF");
        templateButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTemplateConfig(); }
        });
        if (!importingTxt) panel.addView(templateButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46)));
        Button exportButton = makeButton("导出本次生成的 TXT");
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { exportGeneratedCandidate(); }
        });
        LinearLayout.LayoutParams exportParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        exportParams.setMargins(0, dp(6), 0, 0);
        if (!importingTxt) panel.addView(exportButton, exportParams);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        actionRow.setPadding(0, dp(8), 0, 0);
        Button discardButton = makeButton(importingTxt ? "取消" : "放弃");
        Button adjustButton = makeButton("重新调整");
        Button openReaderButton = makeButton(importingTxt ? "导入并打开" : "载入播报器");
        Button[] previewActions = importingTxt
                ? new Button[] { discardButton, openReaderButton }
                : new Button[] { discardButton, adjustButton, openReaderButton };
        for (Button button : previewActions) {
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setSingleLine(false);
            button.setMaxLines(2);
            button.setTextSize(isEnglish() ? 10f : 12f);
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(3), 0, dp(3), 0);
            LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                    0, dp(48), 1f);
            actionParams.setMargins(dp(2), 0, dp(2), 0);
            actionRow.addView(button, actionParams);
        }
        panel.addView(actionRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        AlertDialog previewDialog = new AlertDialog.Builder(this)
                .setTitle(tr(importingTxt ? "TXT 导入预览" : "生成预览")).setView(previewFrame)
                .create();
        dialogHolder[0] = previewDialog;
        generatedPreviewDialog = previewDialog;
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (dialogHolder[0] != null) dialogHolder[0].dismiss();
            }
        });
        adjustButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                restarting[0] = true;
                if (dialogHolder[0] != null) dialogHolder[0].dismiss();
                showGeneratorConfig();
            }
        });
        openReaderButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (importingTxt) {
                    if (commitImportedCandidate(previewCandidate, importName,
                            previewNails, selectedProjectLineMm[0],
                            selectedCircleMm[0])
                            && dialogHolder[0] != null) dialogHolder[0].dismiss();
                } else {
                    if (dialogHolder[0] != null) dialogHolder[0].dismiss();
                    commitGeneratedCandidate();
                }
            }
        });
        previewScroll.setFocusableInTouchMode(true);
        previewScroll.requestFocus();
        previewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface dialog) {
                if (generatedPreviewDialog == dialog) generatedPreviewDialog = null;
            }
        });
        previewDialog.show();
        if (previewDialog.getWindow() != null)
            previewDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void regenerateFromPreview(AlertDialog dialog, boolean[] restarting,
                                       SeekBar nailsBar, SeekBar stepsBar,
                                       CheckBox autoStopBox) {
        if (restarting[0] || dialog == null || !dialog.isShowing()) return;
        int nails = 100 + nailsBar.getProgress() * 10;
        int steps = 1000 + stepsBar.getProgress() * 500;
        boolean projectShapeChanged = nails != generatorNails
                || autoStopBox.isChecked() != generatorAutoStop;
        int generatedLines = generatedCandidate == null
                ? 0 : Math.max(0, generatedCandidate.size() - 1);
        boolean cutsExistingResult = steps < generatedLines;
        if (!projectShapeChanged && !cutsExistingResult) {
            // Raising the cap (or keeping it above an auto-stopped result) cannot change
            // the project already shown. Remember the new cap for a later real regeneration,
            // but do not throw away and recompute an identical preview.
            generatorSteps = steps;
            return;
        }
        restarting[0] = true;
        dialog.dismiss();
        startLocalGeneration(nails, steps, generatorCircleMm, generatorLineMm,
                autoStopBox.isChecked(), generatorCropX, generatorCropY, generatorCropZoom,
                false);
    }

    private String importedPreviewInfoText(ArrayList<Integer> values, int nails,
                                           float lineMm, int circleMm, String name) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        StringBuilder beginning = new StringBuilder();
        int previewCount = Math.min(14, values.size());
        for (int i = 0; i < previewCount; i++) {
            if (i > 0) beginning.append(" → ");
            beginning.append(values.get(i));
        }
        if (values.size() > previewCount) beginning.append(" → …");
        return isEnglish()
                ? name + "\n" + (values.size() - 1) + " strings · " + nails
                    + " nails · thread diameter " + formatLineWidthMm(lineMm)
                    + " · nail circle " + circleMm + " mm"
                    + " · number range " + min + "–" + max + "\n"
                    + beginning + "\nConfirm the preview and starting sequence before importing."
                : name + "\n" + (values.size() - 1) + " 步 · " + nails
                    + " 钉 · 线径 " + formatLineWidthMm(lineMm)
                    + " · 钉位圆 " + circleMm + " mm"
                    + " · 编号范围 " + min + "～" + max + "\n"
                    + beginning + "\n请确认预览和开头顺序无误后再导入。";
    }

    private boolean commitImportedCandidate(ArrayList<Integer> values, String name,
                                            int nails, float lineMm, int circleMm) {
        try { archiveActiveProjectIfNeeded(); }
        catch (IOException e) {
            showError("旧项目自动保存失败，已取消导入：" + safeMessage(e));
            return false;
        }
        pausePlayback();
        sequence.clear();
        sequence.addAll(values);
        currentProjectThumbnail = createFinalThumbnail(values,
                resolvedProjectNails(values, nails), lineMm, circleMm);
        pendingGeneratedThumbnail = null;
        currentIndex = 0;
        importedFileName = name;
        generatorNails = resolvedProjectNails(values, nails);
        generatorLineMm = lineMm;
        generatorCircleMm = circleMm;
        previewUseActualRatio = true;
        previewCustomLineMm = generatorLineMm;
        prefs.edit()
                .putInt(KEY_PROJECT_NAILS, generatorNails)
                .putInt(KEY_PROJECT_CIRCLE_MM, generatorCircleMm)
                .putFloat(KEY_PROJECT_LINE_MM, generatorLineMm)
                .putFloat(KEY_PREVIEW_CUSTOM_LINE_MM, previewCustomLineMm)
                .putBoolean(KEY_PREVIEW_USE_ACTUAL_RATIO, true)
                .apply();
        setActiveAutoProject(null, null);
        try { createActiveAutoProject(); }
        catch (IOException e) {
            showError("新项目自动保存失败：" + safeMessage(e));
        }
        previewVisible = true;
        previewMinimized = false;
        persistSequence();
        updateUi();
        applyPreviewPanelState(true);
        toast("已导入并打开播报器", Toast.LENGTH_SHORT);
        speakCurrent(false);
        return true;
    }

    private String generatedPreviewInfoText() {
        float calculatedMeters = estimateThreadMeters(
                generatedCandidate, generatorNails, generatorCircleMm, 1f);
        float recommendedMeters = estimateThreadMeters(
                generatedCandidate, generatorNails, generatorCircleMm, 1.10f);
        String stopText = isEnglish()
                ? (generatorAutoStop
                    ? ((generatedCandidate.size() - 1 < generatorSteps)
                        ? "Over-darkening protection: stopped early"
                        : "Over-darkening protection: not triggered")
                    : "Over-darkening protection: off")
                : (generatorAutoStop
                    ? ((generatedCandidate.size() - 1 < generatorSteps)
                        ? "自动防全黑：已提前停止"
                        : "自动防全黑：未触发")
                    : "自动防全黑：已关闭");
        return isEnglish()
                ? generatorNails + " nails · " + (generatedCandidate.size() - 1)
                    + " strings · Nail circle " + generatorCircleMm + " mm · Thread diameter "
                    + String.format(Locale.US, "%.2f", generatorLineMm) + " mm\n"
                    + "Estimated total length: " + formatMeters(calculatedMeters)
                    + "; prepare at least " + formatMeters(recommendedMeters)
                    + " (includes 10% reserve)\n" + stopText
                : generatorNails + " 钉 · " + (generatedCandidate.size() - 1)
                    + " 根线 · 钉位圆 " + generatorCircleMm + " mm · 线径 "
                    + String.format(Locale.CHINA, "%.2f", generatorLineMm) + " mm\n"
                    + "弦长合计约 " + formatMeters(calculatedMeters)
                    + "；建议准备至少 " + formatMeters(recommendedMeters)
                    + " 线（已预留 10%）\n" + stopText;
    }

    private float estimateThreadMeters(ArrayList<Integer> values, int nails, int diameterMm, float allowance) {
        if (values == null || values.size() < 2 || nails < 2) return 0f;
        double totalMm = 0d;
        for (int i = 1; i < values.size(); i++) {
            int a = values.get(i - 1), b = values.get(i);
            int gap = Math.abs(a - b) % nails;
            gap = Math.min(gap, nails - gap);
            totalMm += diameterMm * Math.sin(Math.PI * gap / nails);
        }
        return (float) (totalMm * allowance / 1000d);
    }

    private String formatMeters(float value) {
        return value < 10f ? String.format(Locale.CHINA, "%.1f m", value)
                : String.format(Locale.CHINA, "%.0f m", value);
    }

    private void commitGeneratedCandidate() {
        if (generatedCandidate == null || generatedCandidate.size() < 2) return;
        try { archiveActiveProjectIfNeeded(); }
        catch (IOException e) { showError("旧项目自动保存失败，未替换当前项目：" + safeMessage(e)); return; }
        pausePlayback();
        sequence.clear(); sequence.addAll(generatedCandidate); currentIndex = 0;
        currentProjectThumbnail = copyBytes(pendingGeneratedThumbnail);
        setActiveAutoProject(null, null);
        importedFileName = "生成 · " + pendingGeneratorName + " · " + generatorNails + "钉 · " + generatorCircleMm + "mm";
        prefs.edit().putInt(KEY_PROJECT_NAILS, generatorNails)
                .putInt(KEY_PROJECT_CIRCLE_MM, generatorCircleMm)
                .putFloat(KEY_PROJECT_LINE_MM, generatorLineMm).apply();
        try { createActiveAutoProject(); }
        catch (IOException e) {
            showError("新项目自动保存失败：" + safeMessage(e));
        }
        previewVisible = true; previewMinimized = false;
        persistSequence(); updateUi(); applyPreviewPanelState(true);
        toast("已载入播报器", Toast.LENGTH_SHORT);
        speakCurrent(false);
    }

    private void showMoreMenu() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(4), dp(16), dp(4));

        TextView guide = dialogLabel(isEnglish()
                ? "67"
                : "关注塔菲喵");
        guide.setTextSize(12f);
        guide.setTextColor(MUTED);
        guide.setPadding(dp(4), 0, dp(4), dp(8));
        panel.addView(guide);

        final String[] labels = new String[] {
                "🎙 " + tr("播报设置"), "🌐 " + tr("界面语言"),
                "🗂 " + tr("项目管理"), "📄 " + tr("导出当前 TXT"),
                "📐 " + tr("导出钉位图 PDF"), "📤 " + tr("分享应用"),
                "📱 " + tr("微信小程序版"), "💜 " + tr("支持作者"),
                "💬 " + tr("反馈与联系"), "⌚ " + tr("手环控制"),
                "ℹ️ " + tr("关于")
        };
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("更多功能"))
                .setView(panel)
                .setNegativeButton(tr("关闭"), null)
                .create();
        int columns = landscape ? 3 : 2;
        LinearLayout row = null;
        for (int i = 0; i < labels.length; i++) {
            if (i % columns == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                panel.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
            }
            final int action = i;
            Button button = makeButton(labels[i]);
            button.setTextSize(isEnglish() ? 11f : 12.5f);
            button.setSingleLine(false);
            button.setMaxLines(2);
            button.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    dialog.dismiss();
                    openMoreAction(action);
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1f);
            params.setMargins(dp(3), dp(2), dp(3), dp(2));
            row.addView(button, params);
        }
        dialog.show();
    }

    private void openMoreAction(int action) {
        switch (action) {
            case 0: showSettingsDialog(); break;
            case 1: showLanguageDialog(); break;
            case 2: showSaveManager(true); break;
            case 3: exportCurrentSequence(); break;
            case 4: showTemplateConfig(); break;
            case 5: shareInstalledApplication(); break;
            case 6: showMiniProgramDialog(); break;
            case 7: showSupportDialog(); break;
            case 8: showContactDialog(); break;
            case 9: showWearableControlPage(); break;
            default: showAboutDialog(); break;
        }
    }

    private void showWearableControlDialog() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(6), dp(22), dp(4));

        final CheckBox enabled = new CheckBox(this);
        enabled.setText(tr("允许小米手环控制播报器"));
        enabled.setTextColor(FG);
        enabled.setTextSize(16f);
        enabled.setChecked(wearableControlEnabled);
        panel.addView(enabled, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        wearableStatusView = dialogLabel(localizedWearableStatus());
        wearableStatusView.setTextSize(14f);
        wearableStatusView.setTextColor(wearableControlEnabled ? FG : MUTED);
        wearableStatusView.setPadding(dp(4), dp(4), dp(4), dp(10));
        panel.addView(wearableStatusView);

        TextView description = dialogLabel(isEnglish()
                ? "Requires Mi Fitness to stay connected to the band. The watch app and this Android app communicate through Xiaomi's official Wearable SDK; no raw Bluetooth pairing is needed. Swipe on the watch screen or use its buttons to control the reader. Experimental wrist-flick control is off by default on the watch."
                : "需要小米运动健康保持手环连接。手环端通过小米官方 Wearable SDK 与本应用通信，不需要再次进行原始蓝牙配对。可在手环上点击按钮或左右滑动控制；实验性甩腕控制在手环端默认关闭。");
        description.setTextSize(13f);
        description.setTextColor(MUTED);
        description.setLineSpacing(dp(2), 1f);
        panel.addView(description);

        final Button reconnect = makeButton("重新检测连接");
        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!wearableControlEnabled) {
                    toast("请先开启手环控制", Toast.LENGTH_SHORT);
                    return;
                }
                connectWearable(true);
            }
        });
        LinearLayout.LayoutParams reconnectParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        reconnectParams.setMargins(0, dp(10), 0, 0);
        panel.addView(reconnect, reconnectParams);

        final Button launch = makeButton("在手环上打开应用");
        launch.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { launchWearableApp(); }
        });
        LinearLayout.LayoutParams launchParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        launchParams.setMargins(0, dp(8), 0, 0);
        panel.addView(launch, launchParams);

        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                wearableControlEnabled = checked;
                prefs.edit().putBoolean(KEY_WEARABLE_CONTROL, checked).apply();
                lastWearableState = "";
                if (checked) connectWearable(true);
                else {
                    disconnectWearable();
                    setWearableStatus("手环控制未开启");
                }
                reconnect.setEnabled(checked);
                launch.setEnabled(checked);
            }
        });
        reconnect.setEnabled(wearableControlEnabled);
        launch.setEnabled(wearableControlEnabled);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("小米手环控制"))
                .setView(panel)
                .setPositiveButton(tr("完成"), null)
                .create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface dialogInterface) {
                wearableStatusView = null;
            }
        });
        dialog.show();
    }

    /** In-app page (not an Android AlertDialog) for the complete two-way configuration. */
    private void showWearableControlPage() {
        wearablePageVisible = true;
        final LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(52), dp(16), dp(24));
        page.setBackgroundColor(BG);

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        Button back = makeButton("‹ " + tr("返回"));
        TextView title = dialogLabel("⌚ " + tr("手环与播报控制"));
        title.setTextSize(21f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(back, new LinearLayout.LayoutParams(dp(82), dp(44)));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        titleParams.setMargins(dp(10), 0, 0, 0);
        titleRow.addView(title, titleParams);
        page.addView(titleRow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { wearablePageVisible = false; buildUi(); updateUi(); }
        });

        final CheckBox enabled = settingsCheckBox("允许小米手环连接、同步并控制播报器",
                wearableControlEnabled);
        page.addView(enabled);
        wearableStatusView = dialogLabel(localizedWearableStatus());
        wearableStatusView.setTextColor(wearableControlEnabled ? FG : MUTED);
        wearableStatusView.setPadding(dp(5), 0, dp(5), dp(8));
        page.addView(wearableStatusView);

        LinearLayout connectionRow = new LinearLayout(this);
        connectionRow.setOrientation(LinearLayout.HORIZONTAL);
        Button reconnect = makeButton("重新检测");
        Button launch = makeButton("打开手环端");
        Button sync = makeButton("同步全部存档");
        connectionRow.addView(reconnect, wearablePageButtonParams());
        connectionRow.addView(launch, wearablePageButtonParams());
        connectionRow.addView(sync, wearablePageButtonParams());
        page.addView(connectionRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52)));
        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { connectWearable(true); }
        });
        launch.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { launchWearableApp(); }
        });
        sync.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { syncAllArchivesToWearable(); }
        });
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                wearableControlEnabled = checked;
                prefs.edit().putBoolean(KEY_WEARABLE_CONTROL, checked).apply();
                if (checked) connectWearable(true); else disconnectWearable();
                setWearableStatus(checked ? "正在查找已连接的手环…" : "手环控制未开启");
            }
        });

        addWearablePageHeading(page, "手机端播报");
        final CheckBox phoneVibrate = settingsCheckBox("播报钉号时手机震动（默认关闭）",
                phoneVibrationEnabled);
        final CheckBox repeat = settingsCheckBox("每个钉号重复播报两次", repeatTwice);
        final CheckBox duck = settingsCheckBox("播报时降低其他媒体声音", mediaDuckingEnabled);
        final CheckBox volumeKeys = settingsCheckBox("启用音量键控制", volumeKeysEnabled);
        page.addView(phoneVibrate); page.addView(repeat); page.addView(duck); page.addView(volumeKeys);

        final String[] upAction = { volumeUpAction };
        final String[] downAction = { volumeDownAction };
        Button volumeUp = makeButton("音量＋：" + actionLabel(upAction[0]));
        Button volumeDown = makeButton("音量－：" + actionLabel(downAction[0]));
        page.addView(volumeUp, fullWidthButtonParams());
        page.addView(volumeDown, fullWidthButtonParams());
        volumeUp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showActionSelectionDialog("音量＋对应功能", upAction[0],
                        new ActionSelectionCallback() {
                    @Override public void onSelected(String action) {
                        upAction[0] = action;
                        volumeUpAction = action;
                        volumeUp.setText("音量＋：" + actionLabel(action));
                        persistAndSyncConfig();
                    }
                });
            }
        });
        volumeDown.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showActionSelectionDialog("音量－对应功能", downAction[0],
                        new ActionSelectionCallback() {
                    @Override public void onSelected(String action) {
                        downAction[0] = action;
                        volumeDownAction = action;
                        volumeDown.setText("音量－：" + actionLabel(action));
                        persistAndSyncConfig();
                    }
                });
            }
        });

        addWearablePageHeading(page, "手环端体验");
        final CheckBox watchVibrate = settingsCheckBox("切换/播报钉号时手环震动（默认开启）",
                watchVibrationEnabled);
        final CheckBox keepOn = settingsCheckBox("手环应用保持屏幕常亮（默认开启）",
                watchKeepScreenOn);
        page.addView(watchVibrate); page.addView(keepOn);
        final String[] outward = { watchOutwardAction };
        final String[] inward = { watchInwardAction };
        Button outwardButton = makeButton("向内翻腕：" + actionLabel(outward[0]));
        Button inwardButton = makeButton("向外翻腕：" + actionLabel(inward[0]));
        page.addView(outwardButton, fullWidthButtonParams());
        page.addView(inwardButton, fullWidthButtonParams());
        outwardButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                outward[0] = nextAction(outward[0]); watchOutwardAction = outward[0];
                outwardButton.setText("向内翻腕：" + actionLabel(outward[0])); persistAndSyncConfig();
            }
        });
        inwardButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                inward[0] = nextAction(inward[0]); watchInwardAction = inward[0];
                inwardButton.setText("向外翻腕：" + actionLabel(inward[0])); persistAndSyncConfig();
            }
        });

        CompoundButton.OnCheckedChangeListener configListener =
                new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                phoneVibrationEnabled = phoneVibrate.isChecked();
                repeatTwice = repeat.isChecked();
                mediaDuckingEnabled = duck.isChecked();
                volumeKeysEnabled = volumeKeys.isChecked();
                watchVibrationEnabled = watchVibrate.isChecked();
                watchKeepScreenOn = keepOn.isChecked();
                persistAndSyncConfig();
            }
        };
        phoneVibrate.setOnCheckedChangeListener(configListener);
        repeat.setOnCheckedChangeListener(configListener);
        duck.setOnCheckedChangeListener(configListener);
        volumeKeys.setOnCheckedChangeListener(configListener);
        watchVibrate.setOnCheckedChangeListener(configListener);
        keepOn.setOnCheckedChangeListener(configListener);

        TextView note = dialogLabel("配置以手机为持久化主副本；任一端修改后都会立即回传并刷新另一端。"
                + "存档同步使用分片校验，手环离线进度会在重新连接时回传。");
        note.setTextSize(12f); note.setTextColor(MUTED); note.setPadding(dp(5), dp(14), dp(5), 0);
        page.addView(note);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.addView(page); setContentView(scroll);
        if (wearableControlEnabled) { connectWearable(false); sendWearableConfig(); }
    }

    private CheckBox settingsCheckBox(String text, boolean checked) {
        CheckBox box = new CheckBox(this); box.setText(tr(text)); box.setTextColor(FG);
        box.setTextSize(15f); box.setChecked(checked); box.setPadding(dp(3), dp(2), dp(3), dp(2));
        return box;
    }

    private LinearLayout.LayoutParams wearablePageButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(46), 1f);
        params.setMargins(dp(3), dp(3), dp(3), dp(3)); return params;
    }

    private LinearLayout.LayoutParams fullWidthButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        params.setMargins(0, dp(4), 0, 0); return params;
    }

    private void addWearablePageHeading(LinearLayout page, String text) {
        TextView heading = dialogLabel(tr(text)); heading.setTextSize(17f);
        heading.setTypeface(Typeface.DEFAULT_BOLD); heading.setTextColor(ACCENT);
        heading.setPadding(dp(4), dp(18), dp(4), dp(6)); page.addView(heading);
    }

    private String nextAction(String action) {
        String[] actions = {"previous", "next", "toggle", "replay", "faster", "slower", "none"};
        for (int i = 0; i < actions.length; i++)
            if (actions[i].equals(action)) return actions[(i + 1) % actions.length];
        return "previous";
    }

    private String actionLabel(String action) {
        if ("previous".equals(action)) return tr("上一步");
        if ("next".equals(action)) return tr("下一步");
        if ("toggle".equals(action)) return tr("播放/暂停");
        if ("replay".equals(action)) return tr("重播当前");
        if ("faster".equals(action)) return tr("快一点");
        if ("slower".equals(action)) return tr("慢一点");
        return tr("不操作");
    }

    private interface ActionSelectionCallback {
        void onSelected(String action);
    }

    private void showActionSelectionDialog(String title, String current,
                                           final ActionSelectionCallback callback) {
        final String[] values = {"previous", "next", "toggle", "replay", "faster", "slower", "none"};
        final String[] labels = {tr("上一步"), tr("下一步"), tr("播放/暂停"),
                tr("重播当前"), tr("快一点"), tr("慢一点"), tr("不操作")};
        int checked = 0;
        for (int i = 0; i < values.length; i++) if (values[i].equals(current)) checked = i;
        new AlertDialog.Builder(this)
                .setTitle(tr(title))
                .setSingleChoiceItems(labels, checked, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        callback.onSelected(values[which]);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(tr("取消"), null)
                .show();
    }

    private void persistAndSyncConfig() {
        wearableConfigRevision++;
        prefs.edit().putBoolean(KEY_PHONE_VIBRATION, phoneVibrationEnabled)
                .putBoolean(KEY_REPEAT, repeatTwice)
                .putBoolean(KEY_MEDIA_DUCKING, mediaDuckingEnabled)
                .putBoolean(KEY_VOLUME_KEYS, volumeKeysEnabled)
                .putString(KEY_VOLUME_UP_ACTION, volumeUpAction)
                .putString(KEY_VOLUME_DOWN_ACTION, volumeDownAction)
                .putBoolean(KEY_WATCH_VIBRATION, watchVibrationEnabled)
                .putBoolean(KEY_WATCH_KEEP_ON, watchKeepScreenOn)
                .putString(KEY_WATCH_OUTWARD_ACTION, watchOutwardAction)
                .putString(KEY_WATCH_INWARD_ACTION, watchInwardAction).apply();
        sendWearableConfig();
    }

    private void connectWearable(final boolean showToastOnMissing) {
        if (!wearableControlEnabled || activityDestroyed) return;
        try {
            wearableNodeApi = Wearable.getNodeApi(getApplicationContext());
            wearableMessageApi = Wearable.getMessageApi(getApplicationContext());
            wearableAuthApi = Wearable.getAuthApi(getApplicationContext());
            setWearableStatus("正在查找已连接的手环…");
            wearableNodeApi.getConnectedNodes()
                    .addOnSuccessListener(new OnSuccessListener<List<Node>>() {
                        @Override public void onSuccess(List<Node> nodes) {
                            if (!wearableControlEnabled || activityDestroyed) return;
                            if (nodes == null || nodes.isEmpty()) {
                                wearableNodeId = null;
                                wearableListenerRegistered = false;
                                setWearableStatus("未找到已连接的手环，请先打开小米运动健康");
                                if (showToastOnMissing)
                                    toast("未找到已连接的手环", Toast.LENGTH_SHORT);
                                return;
                            }
                            wearableNodeId = nodes.get(0).id;
                            requestWearablePermission();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(Exception error) {
                            setWearableStatus("查找手环失败：" + safeMessage(error));
                        }
                    });
        } catch (RuntimeException error) {
            setWearableStatus("Wearable SDK 初始化失败：" + safeMessage(error));
        }
    }

    private void requestWearablePermission() {
        if (wearableNodeId == null || wearableAuthApi == null) return;
        setWearableStatus("正在请求设备管理权限…");
        wearableAuthApi.requestPermission(wearableNodeId, Permission.DEVICE_MANAGER)
                .addOnSuccessListener(new OnSuccessListener<Permission[]>() {
                    @Override public void onSuccess(Permission[] permissions) {
                        registerWearableListener();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(Exception error) {
                        setWearableStatus("手环权限未授予：" + safeMessage(error));
                    }
                });
    }

    private void registerWearableListener() {
        if (!wearableControlEnabled || wearableNodeId == null
                || wearableMessageApi == null) return;
        setWearableStatus("正在建立应用通信…");
        if (wearableListenerRegistered) {
            setWearableStatus("已连接，可以在手环端控制播报器");
            sendWearableState(true);
            sendWearableConfig();
            return;
        }
        wearableMessageApi.addListener(wearableNodeId, wearableMessageListener)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void ignored) {
                        wearableListenerRegistered = true;
                        setWearableStatus("已连接，可以在手环端控制播报器");
                        sendWearableState(true);
                        sendWearableConfig();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(Exception error) {
                        setWearableStatus("应用通信连接失败：" + safeMessage(error));
                    }
                });
    }

    private void disconnectWearable() {
        if (wearableMessageApi != null && wearableNodeId != null
                && wearableListenerRegistered) {
            try { wearableMessageApi.removeListener(wearableNodeId); }
            catch (RuntimeException ignored) { }
        }
        wearableListenerRegistered = false;
        wearableNodeId = null;
        wearableNodeApi = null;
        wearableMessageApi = null;
        wearableAuthApi = null;
        lastWearableState = "";
    }

    private void launchWearableApp() {
        if (!wearableControlEnabled) {
            toast("请先开启手环控制", Toast.LENGTH_SHORT);
            return;
        }
        if (wearableNodeApi == null || wearableNodeId == null) {
            connectWearable(true);
            toast("正在重新检测手环连接", Toast.LENGTH_SHORT);
            return;
        }
        wearableNodeApi.launchWearApp(wearableNodeId, "/pages/index")
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(Exception error) {
                        setWearableStatus("打开手环应用失败：" + safeMessage(error));
                    }
                });
    }

    private void handleWearableMessage(String nodeId, String payload) {
        if (!wearableControlEnabled || wearableNodeId == null
                || !wearableNodeId.equals(nodeId)) return;
        try {
            JSONObject message = new JSONObject(payload);
            String type = message.optString("type", "command");
            if ("request_state".equals(type)) {
                sendWearableState(true);
                sendWearableConfig();
                return;
            }
            if ("request_config".equals(type)) {
                sendWearableConfig();
                return;
            }
            if ("request_preview".equals(type)) {
                sendWearablePreview(message);
                return;
            }
            if ("config_update".equals(type)) {
                applyWearableConfig(message.optJSONObject("config"));
                sendWearableConfig();
                updateUi();
                return;
            }
            if ("archive_ack".equals(type)) {
                continueWearableTransfer(message.optInt("part", -1));
                return;
            }
            if ("archive_progress".equals(type)) {
                applyProgressFromWearable(message);
                return;
            }
            if ("archive_activate".equals(type)) {
                activateSaveRequestedByWearable(message.optString("archiveId", ""));
                return;
            }
            if (!"command".equals(type)) return;
            String action = message.optString("action", "");
            if ("next".equals(action)) moveNext(true);
            else if ("previous".equals(action)) movePrevious(true);
            else if ("toggle".equals(action)) togglePlayback();
            else if ("play".equals(action) && !isPlaying) togglePlayback();
            else if ("pause".equals(action) && isPlaying) pausePlayback();
            else if ("replay".equals(action)) replayCurrent();
            else if ("faster".equals(action)) adjustSpeechDelay(-QUICK_DELAY_STEP_MS, true);
            else if ("slower".equals(action)) adjustSpeechDelay(QUICK_DELAY_STEP_MS, true);
            else if ("jump".equals(action)) jumpFromWearable(
                    message.optInt("index", currentIndex), message.optInt("requestId", 0));
            else sendWearableState(true);
        } catch (JSONException error) {
            setWearableStatus("收到无法识别的手环消息");
        }
    }

    private void jumpFromWearable(int requestedIndex, int requestId) {
        if (sequence.isEmpty()) { sendWearableJumpResult(requestId, false, currentIndex, 0); sendWearableState(true); return; }
        int target = Math.max(0, Math.min(requestedIndex, sequence.size() - 1));
        pausePlayback();
        int oldIndex = currentIndex;
        currentIndex = target;
        saveProgress();
        pendingStepAnimation = currentIndex >= oldIndex ? 1 : -1;
        updateUi();
        speakCurrent(false);
        sendWearableJumpResult(requestId, true, currentIndex, sequence.size());
        sendWearableState(true);
    }

    private void sendWearableJumpResult(int requestId, boolean success, int index, int total) {
        try {
            JSONObject result = new JSONObject();
            result.put("type", "jump_result");
            result.put("requestId", requestId);
            result.put("success", success);
            result.put("index", success ? index + 1 : 0);
            result.put("total", total);
            sendWearableJson(result, null);
        } catch (JSONException ignored) { }
    }

    private void sendWearablePhoneNextFeedback() {
        if (!watchVibrationEnabled) return;
        try {
            JSONObject feedback = new JSONObject();
            feedback.put("type", "phone_next_feedback");
            sendWearableJson(feedback, null);
        } catch (JSONException ignored) { }
    }

    private void sendWearablePreview(JSONObject request) {
        try {
            ArrayList<Integer> values = sequence;
            int nails = resolvedProjectNails(sequence, generatorNails);
            int index = currentIndex;
            int circleMm = generatorCircleMm;
            float lineMm = generatorLineMm;
            String archiveId = request.optString("archiveId", "");
            if (archiveId.length() > 0) {
                File archive = wearableArchiveFile(archiveId);
                if (archive != null) {
                    SaveRecord save = readSave(archive, true);
                    values = save.values;
                    nails = resolvedProjectNails(values, save.projectNails);
                    index = Math.max(0, Math.min(request.optInt("index", save.index),
                            values.size() - 1));
                    circleMm = save.projectCircleMm;
                    lineMm = save.projectLineMm;
                }
            }
            JSONObject response = new JSONObject();
            response.put("type", "preview_image");
            response.put("archiveId", archiveId);
            response.put("index", values == null || values.isEmpty() ? 0 : index + 1);
            response.put("total", values == null ? 0 : values.size());
            byte[] preview = WearableArchiveCodec.progressPreview(values, nails, index,
                    circleMm, lineMm);
            response.put("data", Base64.encodeToString(preview, Base64.NO_WRAP));
            sendWearableJson(response, null);
        } catch (IOException | JSONException | RuntimeException error) {
            setWearableStatus("生成手环预览失败：" + safeMessage(error));
        }
    }

    private void sendWearableState(boolean force) {
        if (!wearableControlEnabled || !wearableListenerRegistered
                || wearableMessageApi == null || wearableNodeId == null) return;
        try {
            JSONObject state = new JSONObject();
            boolean loaded = !sequence.isEmpty();
            state.put("type", "state");
            state.put("loaded", loaded);
            state.put("playing", isPlaying);
            state.put("index", loaded ? currentIndex + 1 : 0);
            state.put("total", sequence.size());
            state.put("previous", loaded && currentIndex > 0
                    ? sequence.get(currentIndex - 1) : JSONObject.NULL);
            state.put("current", loaded ? sequence.get(currentIndex) : JSONObject.NULL);
            state.put("next", loaded && currentIndex + 1 < sequence.size()
                    ? sequence.get(currentIndex + 1) : JSONObject.NULL);
            String project = localizedProjectLabel(importedFileName);
            if (project.length() > 48) project = project.substring(0, 48);
            state.put("project", project);
            state.put("nails", loaded ? resolvedProjectNails(sequence, generatorNails) : 0);
            state.put("circleMm", loaded ? generatorCircleMm : 0);
            state.put("lineMm", loaded ? generatorLineMm : 0d);
            state.put("estimatedMeters", loaded ? estimateThreadMeters(sequence,
                    resolvedProjectNails(sequence, generatorNails), generatorCircleMm, 1f) : 0d);
            state.put("configRevision", wearableConfigRevision);
            final String payload = state.toString();
            if (!force && payload.equals(lastWearableState)) return;
            lastWearableState = payload;
            wearableMessageApi.sendMessage(wearableNodeId,
                    payload.getBytes(StandardCharsets.UTF_8))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(Exception error) {
                            lastWearableState = "";
                            setWearableStatus("向手环同步失败：" + safeMessage(error));
                        }
                    });
        } catch (JSONException ignored) { }
    }

    private JSONObject wearableConfigJson() throws JSONException {
        JSONObject config = new JSONObject();
        config.put("revision", wearableConfigRevision);
        config.put("repeatTwice", repeatTwice);
        config.put("delayMs", delayMs);
        config.put("speechRate", speechRate);
        config.put("ttsVolume", ttsVolumePercent);
        config.put("mediaDucking", mediaDuckingEnabled);
        config.put("phoneVibration", phoneVibrationEnabled);
        config.put("volumeKeys", volumeKeysEnabled);
        config.put("volumeUpAction", volumeUpAction);
        config.put("volumeDownAction", volumeDownAction);
        config.put("watchVibration", watchVibrationEnabled);
        config.put("watchKeepOn", watchKeepScreenOn);
        config.put("watchOutwardAction", watchOutwardAction);
        config.put("watchInwardAction", watchInwardAction);
        return config;
    }

    private void sendWearableConfig() {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "config_state");
            message.put("config", wearableConfigJson());
            sendWearableJson(message, null);
        } catch (JSONException ignored) { }
    }

    private static String validAction(String value, String fallback) {
        if ("none".equals(value) || "previous".equals(value) || "next".equals(value)
                || "toggle".equals(value) || "replay".equals(value)
                || "faster".equals(value) || "slower".equals(value)) return value;
        return fallback;
    }

    private void applyWearableConfig(JSONObject config) {
        if (config == null) return;
        repeatTwice = config.optBoolean("repeatTwice", repeatTwice);
        delayMs = Math.max(500, Math.min(10_000, config.optInt("delayMs", delayMs)));
        speechRate = (float) Math.max(.5d, Math.min(1.5d,
                config.optDouble("speechRate", speechRate)));
        ttsVolumePercent = Math.max(0, Math.min(100,
                config.optInt("ttsVolume", ttsVolumePercent)));
        mediaDuckingEnabled = config.optBoolean("mediaDucking", mediaDuckingEnabled);
        phoneVibrationEnabled = config.optBoolean("phoneVibration", phoneVibrationEnabled);
        volumeKeysEnabled = config.optBoolean("volumeKeys", volumeKeysEnabled);
        volumeUpAction = validAction(config.optString("volumeUpAction", volumeUpAction), "previous");
        volumeDownAction = validAction(config.optString("volumeDownAction", volumeDownAction), "next");
        watchVibrationEnabled = config.optBoolean("watchVibration", watchVibrationEnabled);
        watchKeepScreenOn = config.optBoolean("watchKeepOn", watchKeepScreenOn);
        watchOutwardAction = validAction(config.optString("watchOutwardAction", watchOutwardAction), "next");
        watchInwardAction = validAction(config.optString("watchInwardAction", watchInwardAction), "previous");
        wearableConfigRevision = Math.max(wearableConfigRevision + 1,
                config.optLong("revision", 0L) + 1L);
        prefs.edit().putBoolean(KEY_REPEAT, repeatTwice)
                .putInt(KEY_DELAY, delayMs).putFloat(KEY_RATE, speechRate)
                .putInt(KEY_TTS_VOLUME_PERCENT, ttsVolumePercent)
                .putBoolean(KEY_MEDIA_DUCKING, mediaDuckingEnabled)
                .putBoolean(KEY_PHONE_VIBRATION, phoneVibrationEnabled)
                .putBoolean(KEY_VOLUME_KEYS, volumeKeysEnabled)
                .putString(KEY_VOLUME_UP_ACTION, volumeUpAction)
                .putString(KEY_VOLUME_DOWN_ACTION, volumeDownAction)
                .putBoolean(KEY_WATCH_VIBRATION, watchVibrationEnabled)
                .putBoolean(KEY_WATCH_KEEP_ON, watchKeepScreenOn)
                .putString(KEY_WATCH_OUTWARD_ACTION, watchOutwardAction)
                .putString(KEY_WATCH_INWARD_ACTION, watchInwardAction).apply();
        if (tts != null && ttsReady) tts.setSpeechRate(speechRate);
        if (!mediaDuckingEnabled) abandonSpeechAudioFocus();
    }

    private void sendWearableJson(JSONObject message, final Runnable success) {
        if (!wearableControlEnabled || !wearableListenerRegistered
                || wearableMessageApi == null || wearableNodeId == null) return;
        wearableMessageApi.sendMessage(wearableNodeId,
                message.toString().getBytes(StandardCharsets.UTF_8))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void ignored) { if (success != null) success.run(); }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(Exception error) {
                        wearableTransferWaiting = false;
                        setWearableStatus("向手环同步失败：" + safeMessage(error));
                    }
                });
    }

    private void syncAllArchivesToWearable() {
        if (!wearableListenerRegistered) {
            toast("请先连接手环", Toast.LENGTH_SHORT);
            connectWearable(true);
            return;
        }
        try { archiveActiveProjectIfNeeded(); }
        catch (IOException error) { showError("同步前保存当前进度失败：" + safeMessage(error)); return; }
        wearableTransferQueue.clear();
        wearableTransferPosition = 0;
        wearableTransferWaiting = false;
        try {
            for (SaveRecord summary : listSaves()) {
                SaveRecord full = readSave(summary.file, true);
                enqueueWearableArchive(full);
            }
            JSONObject complete = new JSONObject();
            complete.put("type", "archive_sync_complete");
            complete.put("part", wearableTransferQueue.size());
            wearableTransferQueue.add(complete);
            if (wearableTransferQueue.size() == 1) {
                toast("当前没有可同步的存档", Toast.LENGTH_SHORT);
                return;
            }
            setWearableStatus("正在同步存档到手环…");
            sendCurrentWearableTransferPart();
        } catch (IOException | JSONException | RuntimeException error) {
            wearableTransferQueue.clear();
            showError("准备手环存档失败：" + safeMessage(error));
        }
    }

    private void enqueueWearableArchive(SaveRecord save) throws JSONException {
        int nails = resolvedProjectNails(save.values, save.projectNails);
        byte[] packed = WearableArchiveCodec.pack(save.values, nails);
        byte[] preview = WearableArchiveCodec.progressPreview(save.values, nails, save.index,
                save.projectCircleMm, save.projectLineMm);
        byte[] thumb = WearableArchiveCodec.tinyThumbnail(save.thumbnailBytes);
        if (thumb.length == 0) thumb = WearableArchiveCodec.tinyThumbnail(preview);
        String archiveId = save.file.getName();
        long crc = WearableArchiveCodec.crc32(packed);
        JSONObject header = new JSONObject();
        header.put("type", "archive_header");
        header.put("archiveId", archiveId);
        header.put("name", save.name);
        header.put("source", save.importedFileName);
        header.put("index", save.index);
        header.put("syncedIndex", save.index);
        header.put("total", save.values.size());
        header.put("current", save.values.isEmpty() ? 0
                : save.values.get(Math.max(0, Math.min(save.index, save.values.size() - 1))));
        header.put("nails", nails);
        header.put("circleMm", save.projectCircleMm);
        header.put("lineMm", save.projectLineMm);
        header.put("bits", WearableArchiveCodec.bitsForNails(nails));
        header.put("bytes", packed.length);
        header.put("crc32", crc);
        header.put("thumbnailBytes", thumb.length);
        header.put("thumbnailCrc32", WearableArchiveCodec.crc32(thumb));
        header.put("previewBytes", preview.length);
        header.put("previewCrc32", WearableArchiveCodec.crc32(preview));
        header.put("estimatedMeters", estimateThreadMeters(save.values, nails,
                save.projectCircleMm, 1f));
        addWearableTransferPart(header);
        enqueueBinaryChunks("archive_chunk", archiveId, packed);
        enqueueBinaryChunks("archive_thumbnail", archiveId, thumb);
        enqueueBinaryChunks("archive_preview", archiveId, preview);
        JSONObject end = new JSONObject();
        end.put("type", "archive_end");
        end.put("archiveId", archiveId);
        end.put("crc32", crc);
        addWearableTransferPart(end);
    }

    private void enqueueBinaryChunks(String type, String archiveId, byte[] bytes)
            throws JSONException {
        for (int offset = 0; offset < bytes.length; offset += WEARABLE_CHUNK_BYTES) {
            int length = Math.min(WEARABLE_CHUNK_BYTES, bytes.length - offset);
            JSONObject chunk = new JSONObject();
            chunk.put("type", type);
            chunk.put("archiveId", archiveId);
            chunk.put("offset", offset);
            chunk.put("data", Base64.encodeToString(bytes, offset, length, Base64.NO_WRAP));
            addWearableTransferPart(chunk);
        }
    }

    private void addWearableTransferPart(JSONObject message) throws JSONException {
        message.put("part", wearableTransferQueue.size());
        wearableTransferQueue.add(message);
    }

    private void sendCurrentWearableTransferPart() {
        if (wearableTransferPosition < 0
                || wearableTransferPosition >= wearableTransferQueue.size()) return;
        wearableTransferWaiting = true;
        sendWearableJson(wearableTransferQueue.get(wearableTransferPosition), null);
    }

    private void continueWearableTransfer(int acknowledgedPart) {
        if (!wearableTransferWaiting || acknowledgedPart != wearableTransferPosition) return;
        wearableTransferWaiting = false;
        wearableTransferPosition++;
        if (wearableTransferPosition >= wearableTransferQueue.size()) {
            wearableTransferQueue.clear();
            wearableTransferPosition = -1;
            setWearableStatus("存档与配置已同步");
            toast("手环存档同步完成", Toast.LENGTH_SHORT);
            return;
        }
        sendCurrentWearableTransferPart();
    }

    private File wearableArchiveFile(String archiveId) {
        if (archiveId == null || archiveId.contains("/") || archiveId.contains("\\")) return null;
        try {
            File candidate = new File(saveDirectory(), archiveId);
            return candidate.isFile() ? candidate : null;
        } catch (IOException ignored) { return null; }
    }

    private void applyProgressFromWearable(JSONObject message) {
        File file = wearableArchiveFile(message.optString("archiveId", ""));
        if (file == null) return;
        try {
            SaveRecord save = readSave(file, true);
            byte[] packed = WearableArchiveCodec.pack(save.values,
                    resolvedProjectNails(save.values, save.projectNails));
            if (WearableArchiveCodec.crc32(packed) != message.optLong("crc32", -1L)) return;
            final int index = Math.max(0, Math.min(message.optInt("index", save.index),
                    save.values.size() - 1));
            int baseIndex = Math.max(0, Math.min(message.optInt("baseIndex", save.index),
                    save.values.size() - 1));
            if (index == baseIndex) return;
            if (save.index == baseIndex) { applyWearableProgress(save, index); return; }
            final SaveRecord conflict = save;
            new AlertDialog.Builder(this).setTitle(tr("手环与手机进度冲突"))
                    .setMessage("手机：第 " + (save.index + 1) + " 步\n手环：第 "
                            + (index + 1) + " 步\n请选择要保留的进度。")
                    .setNegativeButton(tr("保留手机进度"), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            syncAllArchivesToWearable();
                        }
                    })
                    .setPositiveButton(tr("使用手环进度"), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            applyWearableProgress(conflict, index);
                            syncAllArchivesToWearable();
                        }
                    }).show();
        } catch (IOException | RuntimeException ignored) { }
    }

    private void applyWearableProgress(SaveRecord save, int index) {
        try {
            save.index = index;
            writeSave(save.file, save.name, save.importedFileName, save.index,
                    System.currentTimeMillis(), save.values, save.projectNails,
                    save.projectCircleMm, save.projectLineMm, save.thumbnailBytes);
            if (activeAutoProjectFile != null && activeAutoProjectFile.equals(save.file)) {
                currentIndex = index; saveProgress(); updateUi();
            }
        } catch (IOException ignored) { }
    }

    private void activateSaveRequestedByWearable(String archiveId) {
        File file = wearableArchiveFile(archiveId);
        if (file != null) loadSave(file);
        sendWearableState(true);
    }

    private void setWearableStatus(String status) {
        wearableStatus = status;
        if (wearableStatusView != null) {
            wearableStatusView.setText(localizedWearableStatus());
            wearableStatusView.setTextColor(wearableControlEnabled ? FG : MUTED);
        }
    }

    private String localizedWearableStatus() {
        if (!isEnglish()) return wearableStatus;
        if ("手环控制未开启".equals(wearableStatus)) return "Watch control is off";
        if ("正在查找已连接的手环…".equals(wearableStatus))
            return "Looking for a connected watch…";
        if ("未找到已连接的手环，请先打开小米运动健康".equals(wearableStatus))
            return "No connected watch found. Open Mi Fitness first.";
        if ("正在请求设备管理权限…".equals(wearableStatus))
            return "Requesting device permission…";
        if ("正在建立应用通信…".equals(wearableStatus))
            return "Opening the app communication channel…";
        if ("已连接，可以在手环端控制播报器".equals(wearableStatus))
            return "Connected. The watch can now control the reader.";
        return wearableStatus;
    }

    private void showMiniProgramDialog() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(8), dp(22), dp(8));

        TextView message = dialogLabel(isEnglish()
                ? "Don't want to download the Android app, or need to use String Art Helper on a non-Android device? Try our WeChat Mini Program.\n\nSearch WeChat for “绕线画助手”. Some screens and interactions differ, but the core features remain the same. .sar saves and TXT sequences can also be moved between the Android app and Mini Program."
                : "不想下载应用，或想在非 Android 设备上使用？请试试我们的微信小程序版。\n\n在微信中搜索“绕线画助手”即可使用。部分界面与操作体验略有差别，但核心功能不变；.sar 存档和 TXT 序列也可以与 Android 版跨平台互通。");
        message.setTextSize(15f);
        message.setLineSpacing(dp(3), 1f);
        panel.addView(message);

        ImageView miniProgramCode = new ImageView(this);
        miniProgramCode.setImageResource(R.drawable.wechat_miniprogram_code);
        miniProgramCode.setAdjustViewBounds(true);
        miniProgramCode.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        miniProgramCode.setContentDescription(isEnglish()
                ? "String Art Helper WeChat Mini Program code"
                : "绕线画助手微信小程序码");
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(240));
        codeParams.setMargins(0, dp(10), 0, dp(6));
        panel.addView(miniProgramCode, codeParams);

        TextView link = dialogLabel(WECHAT_MINIPROGRAM_LINK);
        link.setTextSize(12f);
        link.setTextIsSelectable(true);
        link.setGravity(Gravity.CENTER);
        panel.addView(link);

        Button copyLink = makeButton(isEnglish()
                ? "Copy Mini Program command"
                : "复制小程序口令");
        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        getSystemService(CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(
                            "绕线画助手微信小程序", WECHAT_MINIPROGRAM_LINK));
                    toast(tr("已复制"), Toast.LENGTH_SHORT);
                }
            }
        });
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        copyParams.setMargins(0, dp(8), 0, 0);
        panel.addView(copyLink, copyParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(panel);
        new AlertDialog.Builder(this)
                .setTitle("📱 " + tr("微信小程序版"))
                .setView(scroll)
                .setPositiveButton(tr("知道了"), null)
                .show();
    }

    private void exportCurrentSequence() {
        if (sequence.size() < 2) {
            toast("还没有可导出的序列", Toast.LENGTH_SHORT);
            return;
        }
        beginSequenceExport(new ArrayList<Integer>(sequence),
                sequenceSourceBaseName(importedFileName));
    }

    private void exportGeneratedCandidate() {
        if (generatedCandidate == null || generatedCandidate.size() < 2) {
            toast("没有可导出的生成结果", Toast.LENGTH_SHORT);
            return;
        }
        beginSequenceExport(new ArrayList<Integer>(generatedCandidate),
                sequenceSourceBaseName(pendingGeneratorName));
    }

    private void beginSequenceExport(ArrayList<Integer> values, String title) {
        pendingExportSequence = values;
        pendingExportNails = resolvedProjectNails(values, generatorNails);
        pendingExportLineMm = generatorLineMm >= MIN_THREAD_MM
                && generatorLineMm <= MAX_THREAD_MM
                ? generatorLineMm : DEFAULT_IMPORTED_THREAD_MM;
        pendingExportCircleMm = generatorCircleMm >= MIN_CIRCLE_MM
                && generatorCircleMm <= MAX_CIRCLE_MM
                ? generatorCircleMm : DEFAULT_IMPORTED_CIRCLE_MM;
        String base = sequenceSourceBaseName(title);
        String parameterSuffix = isEnglish()
                ? "_string_sequence_" + pendingExportNails + "_nails_thread_"
                    + String.format(Locale.US, "%.2f", pendingExportLineMm)
                    + "mm_circle_" + pendingExportCircleMm + "mm.txt"
                : "_绕线序列_" + pendingExportNails + "钉_线径"
                    + String.format(Locale.US, "%.2f", pendingExportLineMm)
                    + "mm_圆径" + pendingExportCircleMm + "mm.txt";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, base + parameterSuffix);
        startActivityForResult(intent, REQUEST_CREATE_SEQUENCE_TXT);
    }

    private void writeSequenceTxt(Uri uri, ArrayList<Integer> values,
                                  int nails, float lineMm, int circleMm) throws IOException {
        if (values == null || values.size() < 2) throw new IOException("没有可导出的序列");
        nails = resolvedProjectNails(values, nails);
        if (!Float.isFinite(lineMm) || lineMm < MIN_THREAD_MM || lineMm > MAX_THREAD_MM)
            lineMm = DEFAULT_IMPORTED_THREAD_MM;
        if (circleMm < MIN_CIRCLE_MM || circleMm > MAX_CIRCLE_MM)
            circleMm = DEFAULT_IMPORTED_CIRCLE_MM;
        java.io.OutputStream output = getContentResolver().openOutputStream(uri);
        if (output == null) throw new IOException("无法创建文件");
        try {
            StringBuilder text = new StringBuilder();
            text.append("# 绕线助手导出\n");
            text.append("# 钉数: ").append(nails).append('\n');
            text.append("# 线径: ").append(String.format(Locale.US, "%.2f", lineMm))
                    .append(" mm\n");
            text.append("# 钉位圆直径: ").append(circleMm).append(" mm\n");
            text.append("# 钉号：0 号正右，顺时针递增\n");
            text.append("# 共 ").append(values.size()).append(" 个钉号\n\n");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) text.append(" → ");
                text.append(values.get(i));
                if ((i + 1) % 16 == 0) text.append('\n');
            }
            output.write(text.toString().getBytes(StandardCharsets.UTF_8));
        } finally { output.close(); }
    }

    private void showTemplateConfig() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(8), dp(22), 0);
        TextView note = dialogLabel(isEnglish()
                ? "The size is the diameter of the circle through the nail centres, not the outer size of the board.\nNumbers are printed on an outer paper ring, so the paper inside the nail circle can be removed after nailing. The template uses the fewest possible A4 pages and includes trim lines, alignment marks and a 100 mm calibration ruler.\nNail 0 is at the rightmost point; numbering increases clockwise."
                : "尺寸指钉子中心组成的圆直径，不是木板外径。\n编号印在圆外纸环上，钉完后可撕掉钉位圆内的纸；模板自动按最少 A4 页数拼接，并带裁切线、对齐标记和 100 mm 校准尺。\n0 号在正右，编号顺时针增加。");
        note.setTextSize(14f);
        note.setLineSpacing(dp(3), 1f);
        panel.addView(note);
        TextView nailsTitle = dialogLabel(isEnglish()
                ? "Number of nails (use the same value as image generation)"
                : "钉子数（与图片生成保持一致即可）");
        nailsTitle.setPadding(0, dp(14), 0, 0); panel.addView(nailsTitle);
        final EditText nailsInput = numericInput(String.valueOf(generatorNails), "100～500"); panel.addView(nailsInput);
        TextView sizeTitle = dialogLabel(isEnglish()
                ? "Nail circle diameter (mm)"
                : "钉位圆直径（mm）");
        sizeTitle.setPadding(0, dp(10), 0, 0); panel.addView(sizeTitle);
        final EditText sizeInput = numericInput(
                String.valueOf(generatorCircleMm), "80～1200"); panel.addView(sizeInput);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("生成钉位模板 PDF")).setView(panel)
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("选择保存位置"), null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                pendingTemplateNails = parseIntOr(nailsInput, -1);
                pendingTemplateDiameterMm = parseIntOr(sizeInput, -1);
                if (pendingTemplateNails < 100 || pendingTemplateNails > 500
                        || pendingTemplateDiameterMm < MIN_CIRCLE_MM
                        || pendingTemplateDiameterMm > MAX_CIRCLE_MM) {
                    showError(isEnglish()
                            ? "Enter valid values: 100–500 nails and a diameter of 80–1200 mm."
                            : "请输入有效范围：钉子数 100～500，直径 80～1200 mm。");
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, isEnglish()
                        ? "String_Art_Nail_Template_" + pendingTemplateNails
                            + "_nails_" + pendingTemplateDiameterMm + "mm.pdf"
                        : "绕线画钉位模板_" + pendingTemplateNails
                            + "钉_" + pendingTemplateDiameterMm + "mm.pdf");
                dialog.dismiss();
                startActivityForResult(intent, REQUEST_CREATE_TEMPLATE_PDF);
            }
        });
    }

    private void writeTemplatePdf(Uri uri, int nails, int diameterMm) throws IOException {
        if (nails < 2 || diameterMm < MIN_CIRCLE_MM || diameterMm > MAX_CIRCLE_MM)
            throw new IOException(isEnglish() ? "Invalid template parameters" : "模板参数无效");
        final float pageWidthMm = 210f, pageHeightMm = 297f;
        final float printableWidthMm = 190f, printableHeightMm = 277f;
        // The outer ring survives after the paper inside the nail circle is removed.
        // Eight millimetres is enough for three-digit labels at the supported densities.
        final float labelRingMm = 8f;
        final float templateSizeMm = diameterMm + labelRingMm * 2f;
        int bestCols = 1, bestRows = 1, bestPages = Integer.MAX_VALUE;
        for (int cols = 1; cols <= 12; cols++) for (int rows = 1; rows <= 12; rows++) {
            if (templateSizeMm / cols <= printableWidthMm
                    && templateSizeMm / rows <= printableHeightMm) {
                int pages = cols * rows;
                if (pages < bestPages || (pages == bestPages && cols > bestCols)) {
                    bestPages = pages; bestCols = cols; bestRows = rows;
                }
            }
        }
        if (bestPages == Integer.MAX_VALUE)
            throw new IOException(isEnglish()
                    ? "The size is too large; the maximum is 1200 mm"
                    : "尺寸过大，最多支持 1200 mm");
        final float pt = 72f / 25.4f;
        final int pageWidth = Math.round(pageWidthMm * pt), pageHeight = Math.round(pageHeightMm * pt);
        final float tileWidth = templateSizeMm / bestCols;
        final float tileHeight = templateSizeMm / bestRows;
        final float marginX = (pageWidthMm - tileWidth) * .5f, marginY = (pageHeightMm - tileHeight) * .5f;
        PdfDocument document = new PdfDocument();
        try {
            for (int row = 0; row < bestRows; row++) for (int col = 0; col < bestCols; col++) {
                int pageNumber = row * bestCols + col + 1;
                PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create());
                Canvas canvas = page.getCanvas();
                canvas.drawColor(Color.WHITE);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(.24f * pt);
                canvas.save();
                canvas.clipRect(marginX * pt, marginY * pt, (marginX + tileWidth) * pt, (marginY + tileHeight) * pt);
                canvas.translate((marginX - col * tileWidth) * pt, (marginY - row * tileHeight) * pt);
                float c = (labelRingMm + diameterMm * .5f) * pt;
                float r = diameterMm * .5f * pt;
                canvas.drawCircle(c, c, r, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextAlign(Paint.Align.CENTER);
                // Labels remain on the removable outer ring. The tile bounds include that
                // ring, so labels at both the template edge and page seams are preserved.
                float arcSpacingMm = (float) (Math.PI * diameterMm / nails);
                float labelTextMm = Math.max(1.35f, Math.min(2.6f, arcSpacingMm * .55f));
                float labelRadius = r + 3f * pt;
                paint.setTextSize(labelTextMm * pt);
                for (int i = 0; i < nails; i++) {
                    double a = Math.PI * 2d * i / nails;
                    float x = c + (float) Math.cos(a) * r;
                    float y = c + (float) Math.sin(a) * r;
                    canvas.drawCircle(x, y, .65f * pt, paint);
                    float lx = c + (float) Math.cos(a) * labelRadius;
                    float ly = c + (float) Math.sin(a) * labelRadius
                            + labelTextMm * .36f * pt;
                    String nailLabel = String.valueOf(i);
                    canvas.drawText(nailLabel, lx, ly, paint);
                }
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(.35f * pt);
                canvas.drawLine(c - 5 * pt, c, c + 5 * pt, c, paint);
                canvas.drawLine(c, c - 5 * pt, c, c + 5 * pt, paint);
                canvas.restore();
                // Visible trim box and corner registration marks make multi-page assembly unambiguous.
                paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(.25f * pt);
                canvas.drawRect(marginX * pt, marginY * pt, (marginX + tileWidth) * pt, (marginY + tileHeight) * pt, paint);
                float mark = 4f * pt;
                canvas.drawLine(marginX * pt - mark, marginY * pt, marginX * pt + mark, marginY * pt, paint);
                canvas.drawLine(marginX * pt, marginY * pt - mark, marginX * pt, marginY * pt + mark, paint);
                canvas.drawLine((marginX + tileWidth) * pt - mark, (marginY + tileHeight) * pt, (marginX + tileWidth) * pt + mark, (marginY + tileHeight) * pt, paint);
                canvas.drawLine((marginX + tileWidth) * pt, (marginY + tileHeight) * pt - mark, (marginX + tileWidth) * pt, (marginY + tileHeight) * pt + mark, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(3.2f * pt);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(isEnglish()
                                ? "String Art Nail Template · " + nails + " nails · Diameter " + diameterMm + " mm"
                                : "绕线画钉位模板 · " + nails + "钉 · 直径" + diameterMm + " mm",
                        8 * pt, 7 * pt, paint);
                canvas.drawText(isEnglish()
                                ? "Page " + pageNumber + " / " + bestPages + " (column "
                                    + (col + 1) + "/" + bestCols + ", row " + (row + 1) + "/" + bestRows + ")"
                                : "第 " + pageNumber + " / " + bestPages + " 页（" + (col + 1)
                                    + "/" + bestCols + " 列，" + (row + 1) + "/" + bestRows + " 行）",
                        8 * pt, 292 * pt, paint);
                paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(.45f * pt);
                canvas.drawRect((pageWidthMm - 108) * pt, 283 * pt, (pageWidthMm - 8) * pt, 287 * pt, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize((isEnglish() ? 2.15f : 2.6f) * pt);
                canvas.drawText(isEnglish()
                                ? "100 mm calibration ruler · Print at Actual size / 100%, then measure this line"
                                : "100 mm 校准尺 · 打印后量此线，必须选择实际大小 / 100%",
                        (pageWidthMm - 58) * pt, 292 * pt, paint);
                document.finishPage(page);
            }
            java.io.OutputStream output = getContentResolver().openOutputStream(uri);
            if (output == null)
                throw new IOException(isEnglish()
                        ? "Could not create the output file"
                        : "无法创建输出文件");
            try { document.writeTo(output); } finally { output.close(); }
        } finally { document.close(); }
    }

    private void showQuickDelayDialog() {
        final SeekBar bar = new SeekBar(this);
        bar.setMax(95);
        bar.setProgress(Math.max(0, Math.min(95, (delayMs - 500) / 100)));
        final TextView label = dialogLabel("每个钉号播报完后等待：" + formatSeconds(delayMs));
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(8), dp(22), 0);
        panel.addView(label);
        panel.addView(bar);
        bar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setText(tr("每个钉号播报完后等待：" + formatSeconds(500 + progress * 100)));
            }
        });
        new AlertDialog.Builder(this).setTitle(tr("快捷：等待间隔")).setView(panel)
                .setNegativeButton(tr("取消"), null).setPositiveButton(tr("保存"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        delayMs = 500 + bar.getProgress() * 100;
                        prefs.edit().putInt(KEY_DELAY, delayMs).apply();
                        wearableConfigRevision++;
                        sendWearableConfig();
                        updateUi();
                    }
                }).show();
    }

    private void adjustSpeechDelay(int deltaMs, boolean showResult) {
        int adjusted = Math.max(MIN_SPEECH_DELAY_MS,
                Math.min(MAX_SPEECH_DELAY_MS, delayMs + deltaMs));
        if (adjusted == delayMs) {
            if (showResult) toast(deltaMs < 0 ? "已经是最快间隔" : "已经是最慢间隔",
                    Toast.LENGTH_SHORT);
            return;
        }
        delayMs = adjusted;
        prefs.edit().putInt(KEY_DELAY, delayMs).apply();
        wearableConfigRevision++;
        sendWearableConfig();
        updateUi();
        if (showResult) toast((deltaMs < 0 ? "已加快，等待 " : "已减慢，等待 ")
                + formatSeconds(delayMs), Toast.LENGTH_SHORT);
    }

    private void showQuickRateDialog() {
        final SeekBar bar = new SeekBar(this);
        bar.setMax(100);
        bar.setProgress(Math.max(0, Math.min(100, Math.round((speechRate - 0.5f) * 100f))));
        final TextView label = dialogLabel("当前语速：" + String.format(Locale.CHINA, "%.2f×", speechRate));
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(8), dp(22), 0);
        panel.addView(label);
        panel.addView(bar);
        bar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                label.setText(tr("当前语速：" + String.format(Locale.CHINA, "%.2f×", 0.5f + progress / 100f)));
            }
        });
        new AlertDialog.Builder(this).setTitle(tr("快捷：播报语速")).setView(panel)
                .setNegativeButton(tr("取消"), null).setPositiveButton(tr("保存"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        speechRate = 0.5f + bar.getProgress() / 100f;
                        if (tts != null && ttsReady) tts.setSpeechRate(speechRate);
                        prefs.edit().putFloat(KEY_RATE, speechRate).apply();
                        wearableConfigRevision++;
                        sendWearableConfig();
                        updateQuickSettingsLabels();
                    }
                }).show();
    }

    private void showSettingsDialog() {
        final LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(8), dp(22), dp(8));
        final ScrollView settingsScroll = new ScrollView(this);
        settingsScroll.setFillViewport(true);
        settingsScroll.addView(panel);

        final CheckBox repeatBox = new CheckBox(this);
        repeatBox.setText(tr("每个钉号重复播报两次"));
        repeatBox.setChecked(repeatTwice);
        panel.addView(repeatBox);

        final TextView ttsVolumeLabel = dialogLabel("TTS 音量：" + ttsVolumePercent + "%");
        ttsVolumeLabel.setPadding(0, dp(10), 0, 0);
        panel.addView(ttsVolumeLabel);
        final SeekBar ttsVolumeBar = new SeekBar(this);
        ttsVolumeBar.setMax(100);
        ttsVolumeBar.setProgress(ttsVolumePercent);
        panel.addView(ttsVolumeBar);
        ttsVolumeBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(
                    SeekBar seekBar, int progress, boolean fromUser) {
                ttsVolumeLabel.setText(tr("TTS 音量：" + progress + "%"));
            }
        });

        final CheckBox volumeBox = new CheckBox(this);
        volumeBox.setText(tr("启用音量键控制"));
        volumeBox.setChecked(volumeKeysEnabled);
        panel.addView(volumeBox);

        final CheckBox phoneVibrationBox = new CheckBox(this);
        phoneVibrationBox.setText(tr("播报钉号时手机震动（默认关闭）"));
        phoneVibrationBox.setChecked(phoneVibrationEnabled);
        panel.addView(phoneVibrationBox);

        final String[] settingsUpAction = { volumeUpAction };
        final String[] settingsDownAction = { volumeDownAction };
        final Button settingsUpButton = makeButton("音量＋：" + actionLabel(settingsUpAction[0]));
        final Button settingsDownButton = makeButton("音量－：" + actionLabel(settingsDownAction[0]));
        panel.addView(settingsUpButton, fullWidthButtonParams());
        panel.addView(settingsDownButton, fullWidthButtonParams());
        settingsUpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showActionSelectionDialog("音量＋对应功能", settingsUpAction[0],
                        new ActionSelectionCallback() {
                    @Override public void onSelected(String action) {
                        settingsUpAction[0] = action;
                        settingsUpButton.setText("音量＋：" + actionLabel(action));
                    }
                });
            }
        });
        settingsDownButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showActionSelectionDialog("音量－对应功能", settingsDownAction[0],
                        new ActionSelectionCallback() {
                    @Override public void onSelected(String action) {
                        settingsDownAction[0] = action;
                        settingsDownButton.setText("音量－：" + actionLabel(action));
                    }
                });
            }
        });

        final TextView mediaHeading = dialogLabel("🎵 媒体声音");
        mediaHeading.setPadding(0, dp(16), 0, dp(3));
        mediaHeading.setTextSize(16f);
        panel.addView(mediaHeading);

        final CheckBox duckingBox = new CheckBox(this);
        duckingBox.setText(tr("播报数字时降低其他媒体声音"));
        duckingBox.setChecked(mediaDuckingEnabled);
        panel.addView(duckingBox);

        final TextView mediaNote = dialogLabel(
                "由 Android 在播报瞬间暂时压低音乐，数字读完后立即恢复；"
                        + "不同播放器的降低幅度可能略有差异。");
        mediaNote.setTextSize(13f);
        mediaNote.setTextColor(MUTED);
        mediaNote.setPadding(0, dp(6), 0, 0);
        panel.addView(mediaNote);

        new AlertDialog.Builder(this)
                .setTitle(tr("播报设置"))
                .setView(settingsScroll)
                .setNegativeButton(tr("取消"), null)
                .setNeutralButton(tr("回到开头"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        pausePlayback();
                        if (!sequence.isEmpty()) {
                            currentIndex = 0;
                            saveProgress();
                            updateUi();
                            speakCurrent(false);
                        }
                    }
                })
                .setPositiveButton(tr("保存"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        repeatTwice = repeatBox.isChecked();
                        ttsVolumePercent = ttsVolumeBar.getProgress();
                        volumeKeysEnabled = volumeBox.isChecked();
                        phoneVibrationEnabled = phoneVibrationBox.isChecked();
                        volumeUpAction = settingsUpAction[0];
                        volumeDownAction = settingsDownAction[0];
                        mediaDuckingEnabled = duckingBox.isChecked();
                        prefs.edit().putBoolean(KEY_REPEAT, repeatTwice)
                                .putInt(KEY_TTS_VOLUME_PERCENT, ttsVolumePercent)
                                .putBoolean(KEY_VOLUME_KEYS, volumeKeysEnabled)
                                .putBoolean(KEY_PHONE_VIBRATION, phoneVibrationEnabled)
                                .putString(KEY_VOLUME_UP_ACTION, volumeUpAction)
                                .putString(KEY_VOLUME_DOWN_ACTION, volumeDownAction)
                                .putBoolean(KEY_MEDIA_DUCKING, mediaDuckingEnabled).apply();
                        if (!mediaDuckingEnabled) abandonSpeechAudioFocus();
                        wearableConfigRevision++;
                        sendWearableConfig();
                    }
                }).show();
    }

    private void showLanguageDialog() {
        final String[] values = {"system", "zh", "en"};
        final String[] labels = {tr("跟随系统"), "简体中文", "English"};
        String current = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_LANGUAGE, "system");
        int checked = "zh".equals(current) ? 1 : ("en".equals(current) ? 2 : 0);
        new AlertDialog.Builder(this)
                .setTitle(tr("界面语言"))
                .setSingleChoiceItems(labels, checked, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                                .putString(KEY_LANGUAGE, values[which]).apply();
                        dialog.dismiss();
                        recreate();
                    }
                })
                .setNegativeButton(tr("取消"), null)
                .show();
    }

    private void showAboutDialog() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(8), dp(20), dp(4));

        TextView intro = dialogLabel(isEnglish()
                ? "🧶 String Art Helper v" + BuildConfig.VERSION_NAME
                    + "\n\n🔒 Free, open source and fully offline. Images, TXT files and project saves are processed only on your device and are never uploaded."
                    + "\n\n⭐ Source code, release notes and issue reporting are available in our GitHub repository. Contributions and suggestions are welcome."
                    + "\n\nAuthor: 牛杂の经济学 · GNU GPL v3.0 only"
                : "🧶 绕线助手 v" + BuildConfig.VERSION_NAME
                    + "\n\n🔒 免费、开源、完全离线（绝对不是没钱租服务器，绝对不是）。图片、TXT 和项目存档只在你的设备上处理，不会上传任何数据。"
                    + "\n\n⭐ 项目源码、版本更新记录与问题反馈均可在 GitHub 仓库查看，也欢迎提出建议或参与完善。"
                    + "\n\n作者：牛杂の经济学 · GNU GPL v3.0 only");
        intro.setTextSize(15f);
        intro.setLineSpacing(dp(3), 1f);
        panel.addView(intro);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        String[] actionLabels = {"💜 " + tr("支持作者"), "📤 " + tr("分享应用"),
                "💬 " + tr("反馈联系")};
        for (int i = 0; i < actionLabels.length; i++) {
            final int action = i;
            Button button = makeButton(actionLabels[i]);
            button.setTextSize(isEnglish() ? 10.5f : 12f);
            button.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (action == 0) showSupportDialog();
                    else if (action == 1) shareInstalledApplication();
                    else showContactDialog();
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
            params.setMargins(dp(2), 0, dp(2), 0);
            actions.addView(button, params);
        }
        panel.addView(actions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        Button details = makeButton("版本与开源信息");
        details.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTechnicalAboutDialog(); }
        });
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        detailParams.setMargins(dp(2), dp(7), dp(2), 0);
        panel.addView(details, detailParams);

        Button github = makeButton("⭐ " + tr("GitHub 开源仓库"));
        github.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openExternalUri(GITHUB_URL); }
        });
        LinearLayout.LayoutParams githubParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        githubParams.setMargins(dp(2), dp(7), dp(2), 0);
        panel.addView(github, githubParams);

        new AlertDialog.Builder(this)
                .setTitle(tr("关于"))
                .setView(panel)
                .setPositiveButton(tr("知道了"), null)
                .show();
    }

    private void showTechnicalAboutDialog() {
        String fingerprint = buildCertificateSha256();
        TextView details = dialogLabel(isEnglish()
                ? "Version: " + BuildConfig.VERSION_NAME
                    + "\nAuthor: 牛杂の经济学"
                    + "\nLicense: GNU GPL v3.0 only"
                    + "\nPackage: " + getPackageName()
                    + "\nBuild: " + (BuildConfig.DEBUG ? "debug/test" : "release")
                    + "\nSigning certificate SHA-256:\n" + fingerprint
                    + "\n\nPlease help protect the free open-source edition. Modified distributions must retain the required legal notices, clearly disclose modifications and never impersonate an official build. If you find an unattributed copy, a closed-source resale or a fake official version, please save evidence and contact the author."
                : "版本：" + BuildConfig.VERSION_NAME
                    + "\n作者：牛杂の经济学"
                    + "\n许可证：GNU GPL v3.0 only"
                    + "\n包名：" + getPackageName()
                    + "\n构建类型：" + (BuildConfig.DEBUG ? "测试版" : "发布版")
                    + "\n安装包签名 SHA-256：\n" + fingerprint
                    + "\n\n也请帮忙维护真正免费开源的版本：修改后再分发时必须保留必要的法律与作者声明，明确标注修改内容，且不得冒充官方构建。如果发现删署名、闭源倒卖或冒充官方的版本，请保留证据并联系作者。");
        details.setTextSize(14f);
        details.setLineSpacing(dp(3), 1f);
        details.setTextIsSelectable(true);
        details.setPadding(dp(22), dp(8), dp(22), dp(8));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(details);
        new AlertDialog.Builder(this)
                .setTitle(tr("版本与开源信息"))
                .setView(scroll)
                .setPositiveButton(tr("知道了"), null)
                .show();
    }

    private void showContactDialog() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(6), dp(18), dp(4));

        Button bilibili = makeButton(contactActionText(tr("Bilibili：牛杂の经济学"), tr("点一下即可跳转至 bilibili")));
        bilibili.setGravity(Gravity.CENTER_VERTICAL);
        bilibili.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openBilibiliProfile(); }
        });
        panel.addView(bilibili, contactButtonParams(0));

        Button email = makeButton(contactActionText(tr("邮箱：241120nzdjjx@gmail.com"), tr("点一下即可跳转至邮箱")));
        email.setGravity(Gravity.CENTER_VERTICAL);
        email.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + CONTACT_EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, "绕线助手反馈");
                try { startActivity(intent); }
                catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, CONTACT_EMAIL, Toast.LENGTH_LONG).show();
                }
            }
        });
        panel.addView(email, contactButtonParams(6));

        Button x = makeButton(contactActionText(tr("推特（X）：@nzdjjx241120"), tr("点一下即可跳转至 X")));
        x.setGravity(Gravity.CENTER_VERTICAL);
        x.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openExternalUri(X_URL); }
        });
        panel.addView(x, contactButtonParams(6));

        Button telegram = makeButton(contactActionText(tr("Telegram：@nzdjjx"), tr("点一下即可跳转至 Telegram")));
        telegram.setGravity(Gravity.CENTER_VERTICAL);
        telegram.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openExternalUri(TELEGRAM_URL); }
        });
        panel.addView(telegram, contactButtonParams(6));

        Button genshin = makeButton(contactActionText(
                tr("或者找作者玩原神🤓☝️ UID：305028021"), tr("点一下即可复制 UID")));
        genshin.setGravity(Gravity.CENTER_VERTICAL);
        genshin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Genshin Impact UID", "305028021"));
                    Toast.makeText(MainActivity.this,
                            tr("已复制到剪贴板，原神启动！"), Toast.LENGTH_SHORT).show();
                }
            }
        });
        panel.addView(genshin, contactButtonParams(6));

        new AlertDialog.Builder(this)
                .setTitle(tr("反馈与联系"))
                .setView(panel)
                .setPositiveButton(tr("知道了"), null)
                .show();
    }

    private LinearLayout.LayoutParams contactButtonParams(int topMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        params.setMargins(0, dp(topMarginDp), 0, 0);
        return params;
    }

    private void showSupportDialog() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(12), dp(6), dp(12), dp(12));
        TextView note = dialogLabel(isEnglish()
                ? "Thank you for supporting continued development. Choose either QR code below. If donating is not convenient, visiting Bilibili and leaving a like or a few coins is already a big help."
                : "感谢你支持这个项目继续完善。下面任选一种方式即可；如果暂时不方便赞助，去 Bilibili 点赞或投几个币也已经是很大的支持。");
        note.setTextSize(13f);
        note.setLineSpacing(dp(3), 1f);
        note.setPadding(dp(4), 0, dp(4), dp(8));
        panel.addView(note);

        TextView alipayTitle = dialogLabel(isEnglish() ? "Alipay" : "支付宝");
        alipayTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        alipayTitle.setGravity(Gravity.CENTER);
        panel.addView(alipayTitle);
        ImageView alipay = new ImageView(this);
        alipay.setImageResource(R.drawable.support_alipay);
        alipay.setAdjustViewBounds(true);
        alipay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        alipay.setContentDescription(isEnglish() ? "Alipay payment QR code" : "支付宝收款码");
        panel.addView(alipay, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView wechatTitle = dialogLabel(isEnglish() ? "WeChat Pay" : "微信支付");
        wechatTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        wechatTitle.setGravity(Gravity.CENTER);
        wechatTitle.setPadding(0, dp(14), 0, 0);
        panel.addView(wechatTitle);
        ImageView wechat = new ImageView(this);
        wechat.setImageResource(R.drawable.support_wechat);
        wechat.setAdjustViewBounds(true);
        wechat.setScaleType(ImageView.ScaleType.FIT_CENTER);
        wechat.setContentDescription(isEnglish() ? "WeChat Pay QR code" : "微信收款码");
        panel.addView(wechat, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(panel);
        new AlertDialog.Builder(this)
                .setTitle(isEnglish() ? "Support the author" : "支持作者")
                .setView(scroll)
                .setPositiveButton(tr("知道了"), null)
                .show();
    }

    private String buildCertificateSha256() {
        try {
            PackageInfo info;
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info = getPackageManager().getPackageInfo(getPackageName(),
                        PackageManager.GET_SIGNING_CERTIFICATES);
                if (info.signingInfo == null) return tr("无法读取");
                signatures = info.signingInfo.hasMultipleSigners()
                        ? info.signingInfo.getApkContentsSigners()
                        : info.signingInfo.getSigningCertificateHistory();
            } else {
                info = getPackageManager().getPackageInfo(getPackageName(),
                        PackageManager.GET_SIGNATURES);
                signatures = info.signatures;
            }
            if (signatures == null || signatures.length == 0) return tr("无法读取");
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(signatures[0].toByteArray());
            StringBuilder text = new StringBuilder(digest.length * 3);
            for (int i = 0; i < digest.length; i++) {
                if (i > 0) text.append(':');
                text.append(String.format(Locale.US, "%02X", digest[i] & 0xFF));
            }
            return text.toString();
        } catch (PackageManager.NameNotFoundException e) {
            return tr("无法读取");
        } catch (NoSuchAlgorithmException e) {
            return tr("无法读取");
        }
    }

    private CharSequence contactActionText(String main, String hint) {
        String full = main + "  " + hint;
        android.text.SpannableString text = new android.text.SpannableString(full);
        int start = main.length() + 2;
        text.setSpan(new android.text.style.RelativeSizeSpan(.72f), start, full.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new android.text.style.ForegroundColorSpan(MUTED), start, full.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private void openExternalUri(String value) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(value))); }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
    }

    private void shareInstalledApplication() {
        String[] splits = getApplicationInfo().splitSourceDirs;
        if (splits != null && splits.length > 0) {
            showError(isEnglish()
                    ? "This installation uses split APKs and cannot be shared as one installable file."
                    : "当前安装使用了拆分 APK，无法直接分享为一个可安装文件。");
            return;
        }
        final File installed = new File(getApplicationInfo().sourceDir);
        final AlertDialog preparing = new AlertDialog.Builder(this)
                .setTitle(isEnglish() ? "Preparing APK…" : "正在准备安装包…")
                .setMessage(isEnglish()
                        ? "Copying the currently installed version. This may take a moment."
                        : "正在复制当前安装的版本，请稍候。")
                .setCancelable(false)
                .create();
        preparing.show();
        generatorExecutor.execute(new Runnable() {
            @Override public void run() {
                try {
                    final File shared = prepareSharedFile(installed,
                            "StringArtHelper-v" + BuildConfig.VERSION_NAME + ".apk");
                    handler.post(new Runnable() {
                        @Override public void run() {
                            if (activityDestroyed) return;
                            preparing.dismiss();
                            shareFile(shared, "application/vnd.android.package-archive",
                                    isEnglish() ? "Share String Art Helper" : "分享绕线助手");
                        }
                    });
                } catch (final IOException e) {
                    handler.post(new Runnable() {
                        @Override public void run() {
                            if (activityDestroyed) return;
                            preparing.dismiss();
                            showError(isEnglish()
                                    ? "Could not prepare the APK for sharing: " + safeMessage(e)
                                    : "准备安装包失败：" + safeMessage(e));
                        }
                    });
                }
            }
        });
    }

    private File prepareSharedFile(File source, String displayName) throws IOException {
        File directory = new File(getCacheDir(), "shared_files");
        if (!directory.exists() && !directory.mkdirs()) throw new IOException("无法创建分享缓存");
        long staleBefore = System.currentTimeMillis() - 24L * 60L * 60L * 1000L;
        File[] oldFiles = directory.listFiles();
        if (oldFiles != null) {
            for (File old : oldFiles) {
                if (old.isFile() && old.lastModified() < staleBefore) old.delete();
            }
        }
        String safeName = safeExportBaseName(stripFileExtension(displayName));
        int dot = displayName.lastIndexOf('.');
        String extension = dot >= 0 ? displayName.substring(dot).replaceAll("[^A-Za-z0-9.]", "") : "";
        File destination = new File(directory,
                System.currentTimeMillis() + "__" + safeName + extension);
        InputStream input = new BufferedInputStream(new FileInputStream(source));
        OutputStream output = new BufferedOutputStream(new FileOutputStream(destination));
        try {
            copyStream(input, output, Integer.MAX_VALUE);
            output.flush();
        } finally {
            try { input.close(); } finally { output.close(); }
        }
        return destination;
    }

    private void shareFile(File file, String mimeType, String chooserTitle) {
        Uri uri = ShareFileProvider.uriFor(this, file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setClipData(ClipData.newRawUri(file.getName(), uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(intent, chooserTitle));
        } catch (ActivityNotFoundException e) {
            showError(isEnglish()
                    ? "No app is available to share this file."
                    : "没有可用于分享文件的应用");
        }
    }

    /** Prefer the installed Bilibili app; the public short link remains a safe fallback. */
    private void openBilibiliProfile() {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BILIBILI_URL));
        appIntent.setPackage("tv.danmaku.bili");
        try { startActivity(appIntent); }
        catch (ActivityNotFoundException noBilibili) { openExternalUri(BILIBILI_URL); }
    }

    private TextView dialogLabel(String text) {
        TextView label = new TextView(this);
        label.setTextColor(Color.WHITE);
        label.setTextSize(16f);
        label.setText(tr(text));
        return label;
    }

    private String tr(String text) {
        return isEnglish() ? EnglishText.translate(text) : text;
    }

    private boolean isEnglish() {
        return "en".equals(getResources().getConfiguration().getLocales().get(0).getLanguage());
    }

    /**
     * Translate only app-generated project metadata. User-provided TXT/image filenames and
     * manually chosen save names remain byte-for-byte unchanged.
     */
    private String localizedProjectLabel(String value) {
        if (!isEnglish() || value == null) return value;
        String result = value;
        boolean appProject = result.startsWith("项目 · ");
        if (appProject) result = "Project · " + result.substring("项目 · ".length());
        boolean generated = result.startsWith("生成 · ")
                || result.startsWith("Project · 生成 · ");
        if (result.startsWith("生成 · "))
            result = "Generated · " + result.substring("生成 · ".length());
        else if (result.startsWith("Project · 生成 · "))
            result = "Project · Generated · " + result.substring("Project · 生成 · ".length());
        if (generated) {
            result = result.replaceAll("([0-9]+)钉", "$1 nails")
                    .replaceAll("([0-9]+)mm", "$1 mm");
        }
        return result;
    }

    private void toast(String text, int duration) {
        Toast.makeText(this, tr(text), duration).show();
    }

    private void showJumpDialog() {
        if (previewAnimationRunning) return;
        releaseHeldPreviewResult();
        if (sequence.isEmpty()) return;
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSelectAllOnFocus(true);
        input.setText(String.valueOf(currentIndex + 1));
        input.setHint("1～" + sequence.size());
        input.setGravity(Gravity.CENTER);
        input.setTextSize(24f);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(10), dp(24), 0);
        TextView explain = dialogLabel("输入要跳到的步骤编号（不是钉号）\n当前：第 "
                + (currentIndex + 1) + " 步，钉号 " + sequence.get(currentIndex));
        explain.setGravity(Gravity.CENTER);
        panel.addView(explain);
        panel.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(62)));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("跳转进度"))
                .setView(panel)
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("跳转"), null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface unused) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        String raw = input.getText().toString().trim();
                        int step;
                        try { step = Integer.parseInt(raw); }
                        catch (NumberFormatException e) {
                            input.setError(tr("请输入数字"));
                            return;
                        }
                        if (step < 1 || step > sequence.size()) {
                            input.setError(isEnglish()
                                    ? "Enter a value from 1 to " + sequence.size()
                                    : "请输入 1～" + sequence.size());
                            return;
                        }
                        dialog.dismiss();
                        jumpWithUndo(step - 1, true);
                    }
                });
                input.requestFocus();
                Window w = dialog.getWindow();
                if (w != null) w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        dialog.show();
    }

    /**
     * Finds every exact occurrence of a short, consecutive nail sequence.  Results are sorted
     * by distance from the current progress so a one- or two-number query remains useful even
     * when the same route occurs many times.
     */
    private void showRecoverPositionDialog() {
        if (previewAnimationRunning) return;
        releaseHeldPreviewResult();
        if (sequence.isEmpty()) { toast("请先导入 TXT 序列", Toast.LENGTH_SHORT); return; }

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint(tr("例如：18 73 146 22"));
        input.setGravity(Gravity.CENTER);
        input.setTextSize(20f);
        input.setMinLines(2);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(8), dp(24), 0);
        TextView explain = dialogLabel("按实际绕过的顺序输入连续几个钉号，可用空格、逗号或换行分隔。结果相同时，离当前进度最近的排在前面。");
        explain.setLineSpacing(dp(2), 1f);
        panel.addView(explain);
        panel.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(92)));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("找回钉号位置"))
                .setView(panel)
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("查找"), null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface unused) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        final ArrayList<Integer> needles = parseRecoveryNails(input.getText().toString());
                        if (needles.isEmpty()) { input.setError(tr("请至少输入一个钉号")); return; }
                        if (needles.size() > 32) { input.setError(tr("一次最多输入 32 个连续钉号")); return; }
                        final ArrayList<Integer> matches = findRecoveryMatches(needles);
                        if (matches.isEmpty()) { input.setError(tr("当前序列中没有找到这段连续钉号")); return; }
                        dialog.dismiss();
                        if (matches.size() == 1) {
                            jumpWithUndo(matches.get(0), true);
                        } else {
                            showRecoveryMatches(matches, needles.size());
                        }
                    }
                });
                input.requestFocus();
                Window w = dialog.getWindow();
                if (w != null) w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        dialog.show();
    }

    private ArrayList<Integer> parseRecoveryNails(String raw) {
        ArrayList<Integer> values = new ArrayList<Integer>();
        Matcher matcher = INTEGER_PATTERN.matcher(raw == null ? "" : raw);
        while (matcher.find()) {
            try { values.add(Integer.parseInt(matcher.group())); }
            catch (NumberFormatException ignored) { }
        }
        return values;
    }

    private ArrayList<Integer> findRecoveryMatches(final ArrayList<Integer> needles) {
        ArrayList<Integer> matches = new ArrayList<Integer>();
        int lastStart = sequence.size() - needles.size();
        for (int start = 0; start <= lastStart; start++) {
            boolean same = true;
            for (int offset = 0; offset < needles.size(); offset++) {
                if (!sequence.get(start + offset).equals(needles.get(offset))) { same = false; break; }
            }
            if (same) matches.add(start + needles.size() - 1);
        }
        Collections.sort(matches, new Comparator<Integer>() {
            @Override public int compare(Integer left, Integer right) {
                int byDistance = Integer.compare(Math.abs(left - currentIndex), Math.abs(right - currentIndex));
                return byDistance != 0 ? byDistance : Integer.compare(left, right);
            }
        });
        return matches;
    }

    private void showRecoveryMatches(final ArrayList<Integer> matches, int enteredCount) {
        final int shown = Math.min(matches.size(), 60);
        String[] labels = new String[shown];
        for (int i = 0; i < shown; i++) {
            int index = matches.get(i);
            int distance = Math.abs(index - currentIndex);
            labels[i] = tr("第 ") + (index + 1) + tr(" 步 · 钉号 ") + sequence.get(index)
                    + (distance == 0 ? tr(" · 当前") : tr(" · 相距 ") + distance + tr(" 步"));
        }
        String title = isEnglish()
                ? matches.size() + " matches (nearest first)"
                : "找到 " + matches.size() + " 个位置 · 最近优先";
        if (enteredCount < 3) title += isEnglish() ? " · choose carefully" : " · 请自行确认";
        if (matches.size() > shown) title += isEnglish() ? " · showing 60" : " · 显示前 60 个";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        jumpWithUndo(matches.get(which), true);
                    }
                })
                .setNegativeButton(tr("取消"), null)
                .show();
    }

    private void jumpWithUndo(final int requestedIndex, boolean speak) {
        if (sequence.isEmpty()) return;
        final int target = Math.max(0, Math.min(requestedIndex, sequence.size() - 1));
        final int oldIndex = currentIndex;
        if (target == oldIndex) { if (speak) replayCurrent(); return; }
        pausePlayback();
        currentIndex = target;
        saveProgress();
        pendingStepAnimation = currentIndex >= oldIndex ? 1 : -1;
        updateUi();
        if (speak) speakCurrent(false);
        sendWearableState(true);
        showJumpUndoDialog(oldIndex, target);
    }

    private void showJumpUndoDialog(final int oldIndex, final int targetIndex) {
        jumpUndoHandler.removeCallbacksAndMessages(null);
        final int[] seconds = {5};
        final AlertDialog undo = new AlertDialog.Builder(this)
                .setTitle(tr("已跳转到第 ") + (targetIndex + 1) + tr(" 步"))
                .setMessage(tr("5 秒后自动确认"))
                .setNegativeButton(tr("撤销"), null)
                .setPositiveButton(tr("确认"), null)
                .create();
        undo.setCancelable(false);
        final Runnable tick = new Runnable() {
            @Override public void run() {
                if (!undo.isShowing()) return;
                undo.setMessage((isEnglish() ? "Confirming in " : "将在 ") + seconds[0]
                        + (isEnglish() ? " seconds" : " 秒后自动确认"));
                if (seconds[0]-- <= 0) { undo.dismiss(); return; }
                jumpUndoHandler.postDelayed(this, 1000L);
            }
        };
        undo.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialog) {
                undo.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        jumpUndoHandler.removeCallbacksAndMessages(null);
                        currentIndex = Math.max(0, Math.min(oldIndex, sequence.size() - 1));
                        saveProgress();
                        pendingStepAnimation = currentIndex >= targetIndex ? 1 : -1;
                        updateUi();
                        speakCurrent(false);
                        sendWearableState(true);
                        undo.dismiss();
                    }
                });
                undo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        jumpUndoHandler.removeCallbacksAndMessages(null);
                        undo.dismiss();
                    }
                });
                tick.run();
            }
        });
        undo.show();
    }

    /** One project manager for both the quick entry and the secondary menu. */
    private void showSaveManager(final boolean quickLoad) {
        final ArrayList<SaveRecord> saves = listSaves();
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(4), dp(20), dp(2));

        TextView current = dialogLabel(projectManagerCurrentText());
        projectManagerCurrentView = current;
        current.setTextSize(14f);
        current.setTextColor(FG);
        current.setPadding(dp(12), dp(10), dp(12), dp(10));
        current.setBackground(roundedBackground(PANEL_2, 12));
        panel.addView(current, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView guide = dialogLabel(isEnglish()
                ? "Auto-resume projects keep updating as you work. Manual snapshots preserve an exact step until you overwrite or delete them."
                : "自动续做会随进度更新；手动存档会固定保留某个节点，除非你主动覆盖或删除。");
        guide.setTextSize(12f);
        guide.setTextColor(MUTED);
        guide.setLineSpacing(dp(2), 1f);
        guide.setPadding(dp(4), dp(8), dp(4), 0);
        panel.addView(guide);

        TextView crossPlatform = dialogLabel(isEnglish()
                ? "🔄 Cross-platform: .sar saves and TXT sequences can be moved between the Android app and the WeChat Mini Program. Search WeChat for “绕线画助手”. The UI differs slightly, but progress, sequences and core parameters remain usable.\n\n⌚ Saves can also be synced to a Xiaomi Smart Band for use without the phone."
                : "🔄 跨平台：Android 与微信小程序版可互相导入、导出 .sar 存档和 TXT 序列。微信搜索“绕线画助手”即可使用；两端 UI 略有差别，但存档进度、序列和核心参数可以继续使用。\n\n⌚ 存档还可同步至小米手环，在手环端脱离手机继续使用。");
        crossPlatform.setTextSize(12f);
        crossPlatform.setTextColor(FG);
        crossPlatform.setLineSpacing(dp(2), 1f);
        crossPlatform.setPadding(dp(10), dp(8), dp(10), dp(8));
        crossPlatform.setBackground(roundedBackground(PANEL_2, 10));
        LinearLayout.LayoutParams crossParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        crossParams.setMargins(0, dp(8), 0, 0);
        panel.addView(crossPlatform, crossParams);

        LinearLayout primaryActions = new LinearLayout(this);
        primaryActions.setOrientation(LinearLayout.HORIZONTAL);
        primaryActions.setBaselineAligned(false);
        Button newSave = makeButton("📌 保存当前");
        newSave.setSingleLine(false);
        newSave.setMaxLines(2);
        newSave.setTextSize(isEnglish() ? 11f : 13f);
        newSave.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { promptNewSave(); }
        });
        if (sequence.isEmpty()) {
            newSave.setEnabled(false);
            newSave.setAlpha(.45f);
        }
        Button importSave = makeButton("📥 导入存档");
        importSave.setSingleLine(false);
        importSave.setMaxLines(2);
        importSave.setTextSize(isEnglish() ? 11f : 13f);
        importSave.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openSavePicker(); }
        });
        Button shareApp = makeButton("📤 分享应用");
        shareApp.setSingleLine(false);
        shareApp.setMaxLines(2);
        shareApp.setTextSize(isEnglish() ? 10.5f : 12.5f);
        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { shareInstalledApplication(); }
        });
        LinearLayout.LayoutParams primaryParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        primaryParams.setMargins(dp(3), 0, dp(3), 0);
        primaryActions.addView(newSave, primaryParams);
        primaryActions.addView(importSave, new LinearLayout.LayoutParams(primaryParams));
        primaryActions.addView(shareApp, new LinearLayout.LayoutParams(primaryParams));
        LinearLayout.LayoutParams primaryRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        primaryRowParams.setMargins(dp(-3), dp(10), dp(-3), dp(4));
        panel.addView(primaryActions, primaryRowParams);

        ArrayList<SaveRecord> automatic = new ArrayList<SaveRecord>();
        ArrayList<SaveRecord> manual = new ArrayList<SaveRecord>();
        for (SaveRecord record : saves) {
            if (record.file.getName().startsWith("project_")) automatic.add(record);
            else manual.add(record);
        }
        addSaveGroup(panel, "🔄 自动续做", automatic);
        addSaveGroup(panel, "📌 手动存档", manual);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(panel);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("项目管理"))
                .setView(scroll)
                .setNegativeButton(tr("关闭"), null)
                .create();
        projectManagerDialog = dialog;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface ignored) {
                if (projectManagerDialog == dialog) {
                    projectManagerDialog = null;
                    projectManagerCurrentView = null;
                }
            }
        });
        dialog.show();
    }

    private String projectManagerCurrentText() {
        return sequence.isEmpty()
                ? "🧶 当前项目\n尚未打开项目\n可从下方存档恢复，或导入其他设备的 .sar 存档"
                : "🧶 当前项目\n" + localizedProjectLabel(importedFileName) + "\n第 "
                    + (currentIndex + 1) + " / " + sequence.size() + " 步";
    }

    private void addSaveGroup(LinearLayout panel, String title, ArrayList<SaveRecord> records) {
        TextView heading = dialogLabel(title);
        heading.setTextSize(14f);
        heading.setTextColor(MUTED);
        heading.setPadding(0, dp(12), 0, dp(4));
        panel.addView(heading);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        panel.addView(rows, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        if (records.isEmpty()) {
            addEmptySaveLabel(rows);
            return;
        }
        for (SaveRecord record : records) rows.addView(makeSaveRow(record));
    }

    private void addEmptySaveLabel(LinearLayout rows) {
        TextView empty = dialogLabel("暂无存档");
        empty.setTextSize(13f);
        empty.setTextColor(MUTED);
        empty.setPadding(dp(8), dp(4), 0, dp(6));
        rows.addView(empty);
    }

    private View makeSaveRow(final SaveRecord save) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        boolean active = activeAutoProjectFile != null && activeAutoProjectFile.equals(save.file);
        SimpleDateFormat fmt = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
        String saveRowText = isEnglish()
                ? (active ? "Current · " : "") + localizedProjectLabel(save.name) + "\nStep "
                    + (save.index + 1) + " / " + save.count + "  " + fmt.format(new Date(save.timestamp))
                : (active ? "当前 · " : "") + save.name + "\n第 "
                    + (save.index + 1) + " / " + save.count + " 步　" + fmt.format(new Date(save.timestamp));
        if (save.thumbnailBytes != null && save.thumbnailBytes.length > 0) {
            Bitmap thumbnail = BitmapFactory.decodeByteArray(
                    save.thumbnailBytes, 0, save.thumbnailBytes.length);
            if (thumbnail != null) {
                ImageView preview = new ImageView(this);
                preview.setImageBitmap(thumbnail);
                preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                preview.setContentDescription(isEnglish()
                        ? "Save thumbnail" : "存档缩略图");
                LinearLayout.LayoutParams previewParams =
                        new LinearLayout.LayoutParams(dp(58), dp(58));
                previewParams.setMargins(0, 0, dp(6), 0);
                row.addView(preview, previewParams);
            }
        }
        Button open = makeButton(saveRowText);
        open.setGravity(Gravity.CENTER_VERTICAL);
        open.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        open.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadSave(save.file); }
        });
        row.addView(open, new LinearLayout.LayoutParams(0, dp(64), 1f));
        Button menu = makeButton("⋮");
        menu.setTextSize(22f);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showSaveActions(save, row); }
        });
        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(dp(48), dp(64));
        menuParams.setMargins(dp(5), 0, 0, 0);
        row.addView(menu, menuParams);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(68));
        rowParams.setMargins(0, dp(2), 0, 0);
        row.setLayoutParams(rowParams);
        return row;
    }

    private void promptNewSave() {
        if (sequence.isEmpty()) return;
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        input.setText(defaultSaveName());
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(22), dp(8), dp(22), 0);
        wrap.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(58)));
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("新建存档"))
                .setView(wrap)
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("保存"), null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface unused) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        String name = input.getText().toString().trim();
                        if (name.length() == 0) { input.setError(tr("请输入存档名")); return; }
                        try {
                            File dir = saveDirectory();
                            File target = new File(dir, "save_" + System.currentTimeMillis() + ".sar");
                            writeCurrentSave(target, name);
                            Toast.makeText(MainActivity.this,
                                    isEnglish() ? "Saved: “" + name + "”" : "已保存：“" + name + "”",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            refreshProjectManager();
                        } catch (IOException e) {
                            input.setError(tr("保存失败：" + safeMessage(e)));
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private String defaultSaveName() {
        String base = sequenceSourceBaseName(importedFileName);
        if (base.length() > 24) base = base.substring(0, 24);
        return isEnglish()
                ? base + " · Step " + (currentIndex + 1)
                : base + " · 第" + (currentIndex + 1) + "步";
    }

    private void exportSaveToFile(SaveRecord save) {
        if (save == null || save.file == null || !save.file.isFile()) {
            showError("要导出的存档已不存在");
            return;
        }
        try {
            // Freeze the exact version selected by the user. Opening the system file picker
            // pauses the activity, which may update or retire an auto-resume project.
            pendingSaveExportFile = prepareSharedFile(save.file, saveExportFileName(save));
        } catch (IOException e) {
            showError("准备导出存档失败：" + safeMessage(e));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, saveExportFileName(save));
        try {
            startActivityForResult(intent, REQUEST_CREATE_SAVE);
        } catch (ActivityNotFoundException e) {
            pendingSaveExportFile = null;
            showError("系统中没有可用于保存文件的应用");
        }
    }

    private String saveExportFileName(SaveRecord save) {
        String base = safeExportBaseName(save.name);
        String step = isEnglish()
                ? "_step_" + (save.index + 1) + "_string_art_save.sar"
                : "_第" + (save.index + 1) + "步_绕线存档.sar";
        return base + step;
    }

    private String safeExportBaseName(String value) {
        String clean = value == null ? "" : value.trim();
        clean = clean.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]+", "_")
                .replaceAll("\\s+", " ").replaceAll("[. ]+$", "");
        if (clean.length() == 0) clean = isEnglish() ? "String_art_project" : "绕线项目";
        if (clean.length() > 60) clean = clean.substring(0, 60).trim();
        return clean;
    }

    private void shareSave(SaveRecord save) {
        if (save == null || save.file == null || !save.file.isFile()) {
            showError("要分享的存档已不存在");
            return;
        }
        try {
            File shared = prepareSharedFile(save.file, saveExportFileName(save));
            shareFile(shared, "application/octet-stream",
                    isEnglish() ? "Share string-art save" : "分享绕线存档");
        } catch (IOException e) {
            showError("准备分享存档失败：" + safeMessage(e));
        }
    }

    private void importSaveFromUri(Uri uri, boolean openAfterImport) {
        File temporary = null;
        try {
            File cache = new File(getCacheDir(), "save_import");
            if (!cache.exists() && !cache.mkdirs()) throw new IOException("无法创建临时目录");
            temporary = new File(cache, "incoming_" + System.currentTimeMillis() + ".sar");
            InputStream input = getContentResolver().openInputStream(uri);
            if (input == null) throw new IOException("无法打开所选文件");
            OutputStream output = new BufferedOutputStream(new FileOutputStream(temporary));
            try {
                copyStream(input, output, MAX_IMPORTED_SAVE_BYTES);
                output.flush();
            } finally {
                try { input.close(); } finally { output.close(); }
            }
            SaveRecord imported = readSave(temporary, true);
            validateImportedSave(imported);
            showImportSaveDialog(imported, openAfterImport);
        } catch (IOException e) {
            showError("无法导入存档：" + safeMessage(e));
        } catch (RuntimeException e) {
            showError("无法导入存档：" + safeMessage(e));
        } finally {
            if (temporary != null && temporary.exists()) temporary.delete();
        }
    }

    private void validateImportedSave(SaveRecord imported) throws IOException {
        if (imported.name == null || imported.name.trim().length() == 0
                || imported.name.length() > 200)
            throw new IOException("存档名称异常");
        if (imported.values == null || imported.values.size() != imported.count
                || imported.index < 0 || imported.index >= imported.count)
            throw new IOException("存档进度异常");
        int nails = resolvedProjectNails(imported.values, imported.projectNails);
        if (nails < 2 || nails > 10_000) throw new IOException("存档钉数异常");
        for (int value : imported.values) {
            if (value < 0 || value >= nails) throw new IOException("存档中存在无效钉号");
        }
        imported.projectNails = nails;
        if (!imported.hasProjectGeometry) {
            // SAR2 never stored physical dimensions. Use stable, visible fallback values
            // rather than leaking whatever project happens to be open during import.
            imported.projectCircleMm = DEFAULT_IMPORTED_CIRCLE_MM;
            imported.projectLineMm = DEFAULT_IMPORTED_THREAD_MM;
        }
        if (imported.thumbnailBytes != null) {
            Bitmap decoded = BitmapFactory.decodeByteArray(imported.thumbnailBytes, 0,
                    imported.thumbnailBytes.length);
            if (decoded == null) throw new IOException("存档缩略图损坏");
            decoded.recycle();
        }
        imported.thumbnailBytes = normalizeThumbnailToMonochrome(imported.thumbnailBytes);
    }

    private void showImportSaveDialog(final SaveRecord imported, final boolean openAfterImport) {
        final SaveRecord duplicate = findManualSaveByName(imported.name);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(6), dp(22), 0);
        if (imported.thumbnailBytes != null && imported.thumbnailBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    imported.thumbnailBytes, 0, imported.thumbnailBytes.length);
            if (bitmap != null) {
                ImageView preview = new ImageView(this);
                preview.setImageBitmap(bitmap);
                preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                panel.addView(preview, new LinearLayout.LayoutParams(dp(96), dp(96)));
            }
        }
        String details = isEnglish()
                ? "“" + localizedProjectLabel(imported.name) + "”\n"
                    + "Step " + (imported.index + 1) + " / " + imported.count + "\n"
                    + imported.projectNails + " nails · "
                    + imported.projectCircleMm + " mm circle · "
                    + String.format(Locale.US, "%.2f mm thread", imported.projectLineMm)
                : "“" + imported.name + "”\n"
                    + "第 " + (imported.index + 1) + " / " + imported.count + " 步\n"
                    + imported.projectNails + " 钉 · 圆径 "
                    + imported.projectCircleMm + " mm · 线径 "
                    + String.format(Locale.US, "%.2f mm", imported.projectLineMm);
        if (duplicate != null) details += isEnglish()
                ? "\n\nA manual snapshot with the same name already exists."
                : "\n\n已有同名手动存档。";
        if (!imported.hasProjectGeometry) details += isEnglish()
                ? "\n\nThis is an older save without physical dimensions. It will use the defaults: 260 mm circle and 0.20 mm thread."
                : "\n\n这是不含物理尺寸的旧版存档，将使用默认值：圆径 260 mm、线径 0.20 mm。";
        TextView info = dialogLabel(details);
        info.setTextSize(14f);
        info.setLineSpacing(dp(3), 1f);
        panel.addView(info);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(tr("导入存档"))
                .setView(panel)
                .setNegativeButton(tr("取消"), null);
        if (duplicate == null) {
            builder.setPositiveButton(tr(openAfterImport ? "导入并打开" : "导入"), new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    finishImportSave(imported, null, false, openAfterImport);
                }
            });
        } else {
            builder.setNeutralButton(tr(openAfterImport ? "保留两份并打开" : "保留两份"), new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    finishImportSave(imported, null, true, openAfterImport);
                }
            });
            builder.setPositiveButton(tr(openAfterImport ? "覆盖并打开" : "覆盖同名存档"), new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    finishImportSave(imported, duplicate.file, false, openAfterImport);
                }
            });
        }
        builder.show();
    }

    private SaveRecord findManualSaveByName(String name) {
        for (SaveRecord save : listSaves()) {
            if (!save.file.getName().startsWith("project_") && save.name.equals(name)) return save;
        }
        return null;
    }

    private void finishImportSave(SaveRecord imported, File overwrite, boolean makeUnique,
                                  boolean openAfterImport) {
        try {
            File target = overwrite != null ? overwrite
                    : new File(saveDirectory(), "save_" + System.currentTimeMillis() + ".sar");
            String name = makeUnique ? uniqueManualSaveName(imported.name) : imported.name;
            byte[] thumbnail = imported.thumbnailBytes;
            if (thumbnail == null) thumbnail = createFinalThumbnail(
                    imported.values, imported.projectNails,
                    imported.projectLineMm, imported.projectCircleMm);
            writeSave(target, name, imported.importedFileName, imported.index,
                    System.currentTimeMillis(), imported.values, imported.projectNails,
                    imported.projectCircleMm, imported.projectLineMm, thumbnail);
            refreshProjectManager();
            if (openAfterImport) {
                loadSave(target);
                return;
            }
            Toast.makeText(this, isEnglish()
                    ? "Imported: “" + localizedProjectLabel(name) + "”"
                    : "已导入：“" + name + "”", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            showError("导入存档失败：" + safeMessage(e));
        }
    }

    private String uniqueManualSaveName(String requested) {
        ArrayList<SaveRecord> saves = listSaves();
        String candidate = requested;
        int suffix = 2;
        boolean duplicate;
        do {
            duplicate = false;
            for (SaveRecord save : saves) {
                if (!save.file.getName().startsWith("project_")
                        && save.name.equals(candidate)) {
                    duplicate = true;
                    candidate = requested + (isEnglish()
                            ? " (" + suffix++ + ")" : "（" + suffix++ + "）");
                    break;
                }
            }
        } while (duplicate);
        return candidate;
    }

    private void showSaveActions(final SaveRecord save, final View saveRow) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(4), dp(20), 0);
        TextView details = dialogLabel(isEnglish()
                ? "Saved progress: step " + (save.index + 1) + " / " + save.count
                    + "\n" + save.projectNails + " nails · "
                    + save.projectCircleMm + " mm circle · "
                    + String.format(Locale.US, "%.2f mm thread", save.projectLineMm)
                : "存档进度：第 " + (save.index + 1) + " / " + save.count + " 步"
                    + "\n" + save.projectNails + " 钉 · 圆径 "
                    + save.projectCircleMm + " mm · 线径 "
                    + String.format(Locale.US, "%.2f mm", save.projectLineMm));
        details.setTextSize(15f);
        details.setTextColor(MUTED);
        details.setPadding(0, 0, 0, dp(10));
        panel.addView(details);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(save.name).setView(panel).setNegativeButton(tr("取消"), null).create();
        Button open = makeButton("读取这个存档");
        open.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialog.dismiss(); loadSave(save.file); }
        });
        panel.addView(open, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)));
        Button overwrite = makeButton("用当前进度覆盖");
        overwrite.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialog.dismiss(); confirmOverwrite(save); }
        });
        if (sequence.isEmpty()) {
            overwrite.setEnabled(false);
            overwrite.setAlpha(.45f);
        }
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        actionParams.setMargins(0, dp(6), 0, 0);
        panel.addView(overwrite, actionParams);
        Button rename = makeButton("重命名");
        rename.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialog.dismiss(); promptRenameSave(save); }
        });
        LinearLayout.LayoutParams renameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        renameParams.setMargins(0, dp(6), 0, 0);
        panel.addView(rename, renameParams);
        Button export = makeButton("导出存档");
        export.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                exportSaveToFile(save);
            }
        });
        LinearLayout.LayoutParams exportParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        exportParams.setMargins(0, dp(6), 0, 0);
        panel.addView(export, exportParams);
        Button share = makeButton("分享存档");
        share.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                shareSave(save);
            }
        });
        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        shareParams.setMargins(0, dp(6), 0, 0);
        panel.addView(share, shareParams);
        Button delete = makeButton("删除这个存档");
        styleButton(delete, Color.rgb(104, 44, 55), Color.rgb(138, 61, 74));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                confirmDeleteSave(save, saveRow);
            }
        });
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        deleteParams.setMargins(0, dp(10), 0, 0);
        panel.addView(delete, deleteParams);
        dialog.show();
    }

    private void loadSave(File file) {
        try {
            SaveRecord record = readSave(file, true);
            archiveActiveProjectIfNeeded();
            pausePlayback();
            sequence.clear();
            sequence.addAll(record.values);
            currentProjectThumbnail = copyBytes(record.thumbnailBytes);
            currentIndex = Math.max(0, Math.min(record.index, sequence.size() - 1));
            importedFileName = record.importedFileName;
            if (record.hasProjectGeometry) {
                generatorNails = resolvedProjectNails(record.values, record.projectNails);
                generatorCircleMm = record.projectCircleMm;
                generatorLineMm = record.projectLineMm;
                if (previewUseActualRatio) previewCustomLineMm = generatorLineMm;
                prefs.edit()
                        .putInt(KEY_PROJECT_NAILS, generatorNails)
                        .putInt(KEY_PROJECT_CIRCLE_MM, generatorCircleMm)
                        .putFloat(KEY_PROJECT_LINE_MM, generatorLineMm)
                        .putFloat(KEY_PREVIEW_CUSTOM_LINE_MM, previewCustomLineMm)
                        .apply();
            }
            setActiveAutoProject(file.getName().startsWith("project_") ? file : null,
                    file.getName().startsWith("project_") ? record.name : null);
            persistSequence();
            updateUi();
            dismissProjectManager();
            Toast.makeText(this,
                    isEnglish() ? "Opened: “" + record.name + "”" : "已读取：“" + record.name + "”",
                    Toast.LENGTH_SHORT).show();
            speakCurrent(false);
        } catch (IOException e) {
            showError("读取存档失败：" + safeMessage(e));
        }
    }

    private void confirmOverwrite(final SaveRecord save) {
        new AlertDialog.Builder(this)
                .setTitle(tr("覆盖存档？"))
                .setMessage(isEnglish()
                        ? "Overwrite “" + localizedProjectLabel(save.name)
                            + "” with the current step " + (currentIndex + 1) + "?"
                        : "用当前第 " + (currentIndex + 1) + " 步覆盖“" + save.name + "”。")
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("覆盖"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        try {
                            writeCurrentSave(save.file, save.name);
                            refreshProjectManager();
                            toast("已覆盖存档", Toast.LENGTH_SHORT);
                        } catch (IOException e) {
                            showError("覆盖失败：" + safeMessage(e));
                        }
                    }
                }).show();
    }

    private void promptRenameSave(final SaveRecord save) {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        input.setText(save.name);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(22), dp(8), dp(22), 0);
        wrap.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(58)));
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tr("重命名存档"))
                .setView(wrap)
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("保存"), null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface unused) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        String name = input.getText().toString().trim();
                        if (name.length() == 0) { input.setError(tr("请输入存档名")); return; }
                        try {
                            SaveRecord full = readSave(save.file, true);
                            writeSave(save.file, name, full.importedFileName, full.index,
                                    full.timestamp, full.values, full.projectNails,
                                    full.projectCircleMm,
                                    full.projectLineMm, full.thumbnailBytes);
                            toast("已重命名", Toast.LENGTH_SHORT);
                            dialog.dismiss();
                            refreshProjectManager();
                        } catch (IOException e) {
                            input.setError(tr("重命名失败：" + safeMessage(e)));
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void confirmDeleteSave(final SaveRecord save, final View saveRow) {
        final boolean deletingCurrentAuto = activeAutoProjectFile != null && activeAutoProjectFile.equals(save.file);
        new AlertDialog.Builder(this)
                .setTitle(tr("删除存档？"))
                .setMessage(isEnglish()
                        ? (deletingCurrentAuto
                            ? "“" + localizedProjectLabel(save.name)
                                + "” is the current auto project. Deleting it will also clear the current project, and it will not be recreated automatically."
                            : "“" + localizedProjectLabel(save.name)
                                + "” cannot be recovered after deletion.")
                        : (deletingCurrentAuto
                            ? "“" + save.name + "”是当前自动项目。删除后会同时清空当前项目，且不会自动复活。"
                            : "“" + save.name + "”删除后无法恢复。"))
                .setNegativeButton(tr("取消"), null)
                .setPositiveButton(tr("删除"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (!save.file.delete()) { showError("删除存档失败"); return; }
                        removeSaveRowImmediately(saveRow);
                        if (deletingCurrentAuto) clearCurrentProjectAfterDelete();
                        toast("已删除", Toast.LENGTH_SHORT);
                    }
                }).show();
    }

    private void removeSaveRowImmediately(View saveRow) {
        if (saveRow == null) return;
        ViewParent parent = saveRow.getParent();
        if (!(parent instanceof LinearLayout)) return;
        LinearLayout rows = (LinearLayout) parent;
        rows.removeView(saveRow);
        if (rows.getChildCount() == 0) addEmptySaveLabel(rows);
    }

    private void clearCurrentProjectAfterDelete() {
        pausePlayback();
        sequence.clear();
        currentIndex = 0;
        importedFileName = "未导入序列";
        currentProjectThumbnail = null;
        pendingGeneratedThumbnail = null;
        setActiveAutoProject(null, null);
        prefs.edit().remove(KEY_SEQUENCE).remove(KEY_INDEX).remove(KEY_FILE_NAME).apply();
        updateUi();
        if (projectManagerCurrentView != null) {
            projectManagerCurrentView.setText(tr(projectManagerCurrentText()));
        }
    }

    private void refreshProjectManager() {
        if (projectManagerDialog == null || !projectManagerDialog.isShowing()) return;
        projectManagerDialog.dismiss();
        handler.postDelayed(new Runnable() {
            @Override public void run() { showSaveManager(false); }
        }, 110);
    }

    private void dismissProjectManager() {
        if (projectManagerDialog != null && projectManagerDialog.isShowing())
            projectManagerDialog.dismiss();
    }

    private File saveDirectory() throws IOException {
        File dir = new File(getFilesDir(), "manual_saves");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("无法创建存档目录");
        return dir;
    }

    private void restoreActiveProjectReference() {
        String filename = prefs.getString(KEY_ACTIVE_PROJECT_FILE, null);
        if (filename == null || filename.length() == 0) return;
        File candidate = new File(new File(getFilesDir(), "manual_saves"), filename);
        if (candidate.isFile()) {
            activeAutoProjectFile = candidate;
            activeAutoProjectName = prefs.getString(KEY_ACTIVE_PROJECT_NAME, "项目");
        } else {
            prefs.edit().remove(KEY_ACTIVE_PROJECT_FILE).remove(KEY_ACTIVE_PROJECT_NAME).apply();
        }
    }

    private void setActiveAutoProject(File file, String name) {
        activeAutoProjectFile = file;
        activeAutoProjectName = name;
        SharedPreferences.Editor edit = prefs.edit();
        if (file == null) edit.remove(KEY_ACTIVE_PROJECT_FILE).remove(KEY_ACTIVE_PROJECT_NAME);
        else edit.putString(KEY_ACTIVE_PROJECT_FILE, file.getName())
                .putString(KEY_ACTIVE_PROJECT_NAME, name == null ? "项目" : name);
        edit.apply();
    }

    private ArrayList<SaveRecord> listSaves() {
        ArrayList<SaveRecord> result = new ArrayList<SaveRecord>();
        try {
            File[] files = saveDirectory().listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isFile() || !f.getName().endsWith(".sar")) continue;
                    try {
                        SaveRecord record = readSave(f, true);
                        if (record.thumbnailBytes == null) {
                            record.thumbnailBytes = createFinalThumbnail(
                                    record.values,
                                    resolvedProjectNails(record.values, record.projectNails),
                                    record.projectLineMm, record.projectCircleMm);
                            if (record.thumbnailBytes != null) {
                                writeSave(record.file, record.name, record.importedFileName,
                                        record.index, record.timestamp, record.values,
                                        resolvedProjectNails(record.values, record.projectNails),
                                        record.projectCircleMm, record.projectLineMm,
                                        record.thumbnailBytes);
                            }
                        } else {
                            byte[] normalized = normalizeThumbnailToMonochrome(
                                    record.thumbnailBytes);
                            if (normalized != record.thumbnailBytes) {
                                record.thumbnailBytes = normalized;
                                writeSave(record.file, record.name, record.importedFileName,
                                        record.index, record.timestamp, record.values,
                                        resolvedProjectNails(record.values, record.projectNails),
                                        record.projectCircleMm, record.projectLineMm,
                                        record.thumbnailBytes);
                                if (activeAutoProjectFile != null
                                        && activeAutoProjectFile.equals(record.file))
                                    currentProjectThumbnail = copyBytes(record.thumbnailBytes);
                            }
                        }
                        record.values = null;
                        result.add(record);
                    }
                    catch (IOException ignored) { }
                }
            }
        } catch (IOException ignored) { }
        Collections.sort(result, new Comparator<SaveRecord>() {
            @Override public int compare(SaveRecord a, SaveRecord b) {
                return a.timestamp == b.timestamp ? 0 : (a.timestamp < b.timestamp ? 1 : -1);
            }
        });
        return result;
    }

    private void writeCurrentSave(File file, String name) throws IOException {
        writeSave(file, name, importedFileName, currentIndex, System.currentTimeMillis(), sequence,
                resolvedProjectNails(sequence, generatorNails),
                generatorCircleMm, generatorLineMm, currentProjectThumbnail);
    }

    /**
     * Bind a newly imported/generated sequence to an auto-resume file immediately.
     * Waiting for the first step change or onPause left a crash window where a new project
     * existed only in preferences and was missing from Project Manager.
     */
    private void createActiveAutoProject() throws IOException {
        if (sequence.size() < 2) return;
        File dir = saveDirectory();
        long timestamp = System.currentTimeMillis();
        File target = new File(dir, "project_" + timestamp + ".sar");
        int suffix = 2;
        while (target.exists()) {
            target = new File(dir, "project_" + timestamp + "_" + suffix++ + ".sar");
        }
        String name = automaticProjectName(importedFileName);
        writeSave(target, name, importedFileName, currentIndex,
                timestamp, sequence,
                resolvedProjectNails(sequence, generatorNails),
                generatorCircleMm, generatorLineMm, currentProjectThumbnail);
        setActiveAutoProject(target, name);
    }

    private String automaticProjectName(String sourceName) {
        String base = sequenceSourceBaseName(
                sourceName == null ? "绕线项目" : sourceName);
        if (base.length() > 24) base = base.substring(0, 24);
        return "项目 · " + base;
    }

    /**
     * A project is the whole sequence plus its current position.  The active project stays in
     * preferences for fast resume; it is copied here only when another project replaces it.
     * Near the end, retaining it creates clutter and provides almost no recovery value.
     */
    private void archiveActiveProjectIfNeeded() throws IOException {
        if (sequence.size() < 2) return;
        int remaining = Math.max(0, sequence.size() - 1 - currentIndex);
        int discardThreshold = Math.max(3, (int) Math.ceil(sequence.size() * .001d));
        if (remaining <= discardThreshold) {
            if (activeAutoProjectFile != null && activeAutoProjectFile.exists()) activeAutoProjectFile.delete();
            setActiveAutoProject(null, null);
            return;
        }
        if (activeAutoProjectFile != null && activeAutoProjectFile.exists()) {
            writeSave(activeAutoProjectFile, activeAutoProjectName, importedFileName, currentIndex,
                    System.currentTimeMillis(), sequence,
                    resolvedProjectNails(sequence, generatorNails),
                    generatorCircleMm, generatorLineMm, currentProjectThumbnail);
            return;
        }
        createActiveAutoProject();
    }

    private void writeSave(File file, String name, String imported, int index,
                           long timestamp, ArrayList<Integer> values, int projectNails,
                           int projectCircleMm,
                           float projectLineMm, byte[] thumbnailBytes) throws IOException {
        AtomicFile atomic = new AtomicFile(file);
        FileOutputStream raw = null;
        try {
            raw = atomic.startWrite();
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(raw));
            out.writeUTF(SAVE_MAGIC);
            out.writeUTF(name);
            out.writeUTF(imported == null ? "绕线序列" : imported);
            out.writeInt(index);
            out.writeLong(timestamp);
            out.writeInt(projectNails);
            out.writeInt(projectCircleMm);
            out.writeFloat(projectLineMm);
            int thumbnailLength = thumbnailBytes == null ? 0 : thumbnailBytes.length;
            if (thumbnailLength > MAX_SAVE_THUMBNAIL_BYTES)
                throw new IOException("存档缩略图过大");
            out.writeInt(thumbnailLength);
            if (thumbnailLength > 0) out.write(thumbnailBytes);
            out.writeInt(values.size());
            for (int value : values) out.writeInt(value);
            out.flush();
            atomic.finishWrite(raw);
            raw = null;
        } catch (IOException e) {
            if (raw != null) atomic.failWrite(raw);
            throw e;
        } catch (RuntimeException e) {
            if (raw != null) atomic.failWrite(raw);
            throw e;
        }
    }

    private SaveRecord readSave(File file, boolean includeValues) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        try {
            String magic = in.readUTF();
            boolean legacy = SAVE_MAGIC_LEGACY.equals(magic);
            boolean geometryOnly = SAVE_MAGIC_GEOMETRY.equals(magic);
            boolean thumbnailFormat = SAVE_MAGIC.equals(magic);
            if (!legacy && !geometryOnly && !thumbnailFormat)
                throw new IOException("存档格式不支持");
            SaveRecord record = new SaveRecord();
            record.file = file;
            record.name = in.readUTF();
            record.importedFileName = in.readUTF();
            record.index = in.readInt();
            record.timestamp = in.readLong();
            record.hasProjectGeometry = !legacy;
            if (record.hasProjectGeometry) {
                record.projectNails = in.readInt();
                record.projectCircleMm = in.readInt();
                record.projectLineMm = in.readFloat();
                if (record.projectNails < 2 || record.projectNails > 10_000
                        || record.projectCircleMm < 1
                        || !Float.isFinite(record.projectLineMm)
                        || record.projectLineMm < MIN_THREAD_MM
                        || record.projectLineMm > MAX_THREAD_MM)
                    throw new IOException("存档物理参数异常");
            } else {
                // SAR2 did not contain physical project parameters. Preserve the old behavior
                // for those files; the next save will upgrade them to SAR3.
                record.projectNails = generatorNails;
                record.projectCircleMm = generatorCircleMm;
                record.projectLineMm = generatorLineMm;
            }
            if (thumbnailFormat) {
                int thumbnailLength = in.readInt();
                if (thumbnailLength < 0 || thumbnailLength > MAX_SAVE_THUMBNAIL_BYTES)
                    throw new IOException("存档缩略图异常");
                if (thumbnailLength > 0) {
                    record.thumbnailBytes = new byte[thumbnailLength];
                    in.readFully(record.thumbnailBytes);
                }
            }
            record.count = in.readInt();
            if (record.count < 2 || record.count > MAX_SEQUENCE_LENGTH) throw new IOException("存档数据异常");
            if (includeValues) {
                record.values = new ArrayList<Integer>(record.count);
                for (int i = 0; i < record.count; i++) record.values.add(in.readInt());
            }
            return record;
        } finally { in.close(); }
    }

    private String formatSeconds(int milliseconds) {
        return tr(String.format(Locale.CHINA, "%.1f 秒", milliseconds / 1000f));
    }

    private void recordForwardStepTiming() {
        long now = SystemClock.elapsedRealtime();
        if (lastForwardStepAtMs > 0L) {
            long duration = now - lastForwardStepAtMs;
            // Ignore accidental double taps and long pauses/background time. The estimate should
            // describe active winding pace, not how long the project has existed.
            if (duration >= 500L && duration <= 60_000L) {
                if (observedStepSamples == 0) observedStepDurationMs = duration;
                else observedStepDurationMs = observedStepDurationMs * .75d + duration * .25d;
                observedStepSamples++;
            }
        }
        lastForwardStepAtMs = now;
    }

    private void resetForwardStepClock() {
        lastForwardStepAtMs = 0L;
    }

    private long estimatedStepDurationMs() {
        if (observedStepSamples > 0) return Math.max(500L, Math.round(observedStepDurationMs));
        // Before enough real steps are observed, use the selected post-speech delay plus a small
        // TTS allowance. This gives a useful first estimate without inventing saved history.
        double speechAllowance = 700d / Math.max(.5f, speechRate);
        if (repeatTwice) speechAllowance *= 1.8d;
        return Math.max(500L, Math.round(delayMs + speechAllowance));
    }

    private String progressAndEstimatedTimeText() {
        int remaining = Math.max(0, sequence.size() - 1 - currentIndex);
        long estimatedMs;
        try {
            estimatedMs = Math.multiplyExact((long) remaining, estimatedStepDurationMs());
        } catch (ArithmeticException ignored) {
            estimatedMs = Long.MAX_VALUE;
        }
        String estimate = formatEstimatedTime(estimatedMs);
        if (isEnglish()) {
            String progress = "Step " + (currentIndex + 1) + " / " + sequence.size();
            return landscape ? progress + " · " + estimate : progress + "\n" + estimate;
        }
        String progress = "第 " + (currentIndex + 1) + " / " + sequence.size() + " 步";
        return landscape ? progress + " · " + estimate : progress + "\n" + estimate;
    }

    private String formatEstimatedTime(long milliseconds) {
        if (milliseconds < 60_000L) return isEnglish()
                ? "Estimated under one minute" : "预计少于一分钟";
        long minutes = Math.max(1L, (milliseconds + 59_999L) / 60_000L);
        if (minutes < 60L) return isEnglish()
                ? "Estimated " + minutes + (minutes == 1L ? " minute" : " minutes")
                : "预计 " + minutes + " 分钟";
        long hours = minutes / 60L;
        long remainder = minutes % 60L;
        if (isEnglish()) return "Estimated " + hours + (hours == 1L ? " hour" : " hours")
                + (remainder == 0L ? "" : " " + remainder + " min");
        return "预计 " + hours + " 小时" + (remainder == 0L ? "" : " " + remainder + " 分钟");
    }

    private String formatLineWidthMm(float widthMm) {
        return String.format(Locale.US, "%.2f mm", widthMm);
    }

    private void persistPreviewLineWidth() {
        prefs.edit()
                .putFloat(KEY_PREVIEW_CUSTOM_LINE_MM, previewCustomLineMm)
                .putBoolean(KEY_PREVIEW_USE_ACTUAL_RATIO, previewUseActualRatio)
                .apply();
    }

    private String previewLineWidthLabel(boolean actualRatio, float customLineMm) {
        return previewLineWidthLabel(actualRatio, customLineMm, generatorLineMm);
    }

    private String previewLineWidthLabel(boolean actualRatio, float customLineMm,
                                         float projectLineMm) {
        if (isEnglish()) {
            return actualRatio
                    ? "Preview thread diameter  Current: actual project value ("
                        + formatLineWidthMm(projectLineMm) + ")"
                    : "Preview thread diameter  Current: custom "
                        + formatLineWidthMm(customLineMm);
        }
        return actualRatio
                ? "预览线径　当前：项目实际线径（" + formatLineWidthMm(projectLineMm) + "）"
                : "预览线径　当前：自定义 " + formatLineWidthMm(customLineMm);
    }

    private void persistSequence() {
        StringBuilder builder = new StringBuilder(sequence.size() * 4);
        for (int i = 0; i < sequence.size(); i++) {
            if (i > 0) builder.append(',');
            builder.append(sequence.get(i));
        }
        prefs.edit().putString(KEY_SEQUENCE, builder.toString())
                .putInt(KEY_INDEX, currentIndex)
                .putString(KEY_FILE_NAME, importedFileName).apply();
    }

    private void restoreSequence() {
        String saved = prefs.getString(KEY_SEQUENCE, "");
        if (saved == null || saved.length() == 0) return;
        String[] parts = saved.split(",");
        try {
            for (String part : parts) if (part.length() > 0) sequence.add(Integer.parseInt(part));
            importedFileName = prefs.getString(KEY_FILE_NAME, "已保存的绕线序列");
            currentIndex = prefs.getInt(KEY_INDEX, 0);
            currentIndex = Math.max(0, Math.min(currentIndex, sequence.size() - 1));
        } catch (RuntimeException e) {
            sequence.clear();
            currentIndex = 0;
            prefs.edit().remove(KEY_SEQUENCE).remove(KEY_INDEX).apply();
        }
    }

    private void saveProgress() {
        prefs.edit().putInt(KEY_INDEX, currentIndex).apply();
        // Keep the project list aligned with the first change after launch too,
        // not just after the next background/replace event.
        try { archiveActiveProjectIfNeeded(); }
        catch (IOException ignored) { }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (previewAnimationRunning) return true;
        if (event.getAction() == KeyEvent.ACTION_DOWN) releaseHeldPreviewResult();
        if (event.getAction() == KeyEvent.ACTION_DOWN && !sequence.isEmpty()) {
            int keyCode = event.getKeyCode();
            if (volumeKeysEnabled && keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    && performMappedAction(volumeUpAction)) return true;
            if (volumeKeysEnabled && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                    && performMappedAction(volumeDownAction)) return true;
            if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
                togglePlayback(); return true;
            }
            if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) { moveNext(true); return true; }
            if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) { movePrevious(true); return true; }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean nowLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (nowLandscape == landscape) return;
        landscape = nowLandscape;
        buildUi();
        updateUi();
        if (previewAnimationRunning) lockControlsForPreviewAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        resetForwardStepClock();
        finishFullPreviewAnimation(false);
        pausePlayback();
        saveProgress();
        try { archiveActiveProjectIfNeeded(); }
        catch (IOException ignored) { }
    }

    @Override
    public void onBackPressed() {
        if (wearablePageVisible) {
            wearablePageVisible = false;
            buildUi();
            updateUi();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        activityDestroyed = true;
        disconnectWearable();
        finishFullPreviewAnimation(false);
        generatorCancelled.set(true);
        generatorExecutor.shutdownNow();
        handler.removeCallbacksAndMessages(null);
        if (generatorProgressDialog != null) generatorProgressDialog.dismiss();
        if (generatorConfigDialog != null) generatorConfigDialog.dismiss();
        if (fullscreenDialog != null) fullscreenDialog.dismiss();
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
        abandonSpeechAudioFocus();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    private void showError(String message) {
        new AlertDialog.Builder(this).setTitle(tr("出现问题")).setMessage(tr(message))
                .setPositiveButton(tr("知道了"), null).show();
    }

    private String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.trim().length() == 0
                ? throwable.getClass().getSimpleName() : message;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class StringArtPreview extends View {
        private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint completed = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint current = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint nail = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint currentNail = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pinIndexLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float zoom = 1f, panX, panY, lastX, lastY, lastDistance;
        private boolean pinching;

        StringArtPreview(MainActivity context) {
            super(context);
            background.setColor(PREVIEW_BG);
            completed.setColor(Color.argb(54, 0, 0, 0));
            completed.setStyle(Paint.Style.STROKE);
            current.setColor(ACCENT);
            current.setStyle(Paint.Style.STROKE);
            rim.setColor(Color.rgb(112, 112, 126));
            rim.setStrokeWidth(dp(1));
            rim.setStyle(Paint.Style.STROKE);
            nail.setColor(Color.rgb(104, 104, 116));
            currentNail.setColor(ACCENT);
            label.setColor(Color.rgb(80, 80, 92));
            label.setTextSize(dp(11));
            label.setTextAlign(Paint.Align.CENTER);
            pinIndexLabel.setColor(Color.rgb(70, 70, 82));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(PREVIEW_BG);
            float width = getWidth();
            float height = getHeight();
            if (width <= 2 || height <= 2) return;
            clampPan();
            float cx = width / 2f + panX;
            float cy = height / 2f + panY;
            float r = Math.max(1f, Math.min(width, height) / 2f - dp(22)) * zoom;
            float selectedLineMm = previewUseActualRatio ? generatorLineMm : previewCustomLineMm;
            float baseWidth = Math.max(.12f,
                    (2f * r) * selectedLineMm / Math.max(1f, generatorCircleMm));
            completed.setStrokeWidth(baseWidth);
            current.setStrokeWidth(baseWidth * 1.5f);
            canvas.drawCircle(cx, cy, r, rim);
            if (sequence.isEmpty()) {
                canvas.drawText(tr("导入序列后显示实时效果"), cx, cy, label);
                return;
            }
            int max = 0;
            for (int value : sequence) if (value > max) max = value;
            int pins = Math.max(2, max + 1);

            int renderIndex = previewRenderIndex();
            for (int i = 1; i < renderIndex && i < sequence.size(); i++)
                drawSegment(canvas, sequence.get(i - 1), sequence.get(i), pins, cx, cy, r, completed);
            if (renderIndex > 0 && renderIndex < sequence.size())
                drawSegment(canvas, sequence.get(renderIndex - 1), sequence.get(renderIndex),
                        pins, cx, cy, r, current);

            NailIndexRenderer.draw(canvas, pins, cx, cy, r,
                    getResources().getDisplayMetrics().density, nail, pinIndexLabel);
            int active = sequence.get(Math.max(0, Math.min(renderIndex, sequence.size() - 1)));
            float[] a = pointFor(active, pins, cx, cy, r);
            canvas.drawCircle(a[0], a[1], dp(4), currentNail);

            canvas.drawText(tr("第 " + (renderIndex + 1) + " / " + sequence.size() + " 步"),
                    cx, height - dp(7), label);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() >= 2) {
                pinching = true; lastDistance = touchDistance(event); return true;
            }
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) { lastX=event.getX(); lastY=event.getY(); pinching=false; return true; }
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_MOVE) {
                if (pinching && event.getPointerCount() >= 2) {
                    float d=touchDistance(event); if(lastDistance>0) zoom=Math.max(1f,Math.min(5f,zoom*d/lastDistance)); lastDistance=d;
                } else { panX += event.getX()-lastX; panY += event.getY()-lastY; lastX=event.getX(); lastY=event.getY(); }
                clampPan();
                invalidate(); return true;
            }
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP
                    || event.getActionMasked() == android.view.MotionEvent.ACTION_CANCEL
                    || event.getActionMasked() == android.view.MotionEvent.ACTION_POINTER_UP) {
                pinching=false;
                if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) performClick();
            }
            return true;
        }

        @Override public boolean performClick() { super.performClick(); return true; }

        private float touchDistance(android.view.MotionEvent e) { float x=e.getX(0)-e.getX(1), y=e.getY(0)-e.getY(1); return (float)Math.sqrt(x*x+y*y); }

        private void clampPan() {
            float width = getWidth(), height = getHeight();
            if (width <= 0f || height <= 0f) return;
            float baseRadius = Math.max(1f, Math.min(width, height) / 2f - dp(22));
            float radius = baseRadius * zoom;
            float edgeAllowance = dp(12);
            float maxPan = Math.max(edgeAllowance, radius - baseRadius + edgeAllowance);
            panX = Math.max(-maxPan, Math.min(maxPan, panX));
            panY = Math.max(-maxPan, Math.min(maxPan, panY));
        }

        private void drawSegment(Canvas canvas, int from, int to, int pins,
                                 float cx, float cy, float r, Paint paint) {
            if (from < 0 || to < 0 || from >= pins || to >= pins) return;
            float[] a = pointFor(from, pins, cx, cy, r);
            float[] b = pointFor(to, pins, cx, cy, r);
            canvas.drawLine(a[0], a[1], b[0], b[1], paint);
        }

        private float[] pointFor(int pin, int pins, float cx, float cy, float r) {
            double angle = 2.0 * Math.PI * pin / pins;
            return new float[] {(float) (cx + Math.cos(angle) * r),
                    (float) (cy + Math.sin(angle) * r)};
        }
    }

    private static final class SaveRecord {
        File file;
        String name;
        String importedFileName;
        int index;
        int count;
        int projectNails;
        int projectCircleMm;
        float projectLineMm;
        boolean hasProjectGeometry;
        byte[] thumbnailBytes;
        long timestamp;
        ArrayList<Integer> values;
    }

    private static final class TxtMetadata {
        final int nails;
        final float lineMm;
        final int circleMm;

        TxtMetadata(int nails, float lineMm, int circleMm) {
            this.nails = nails;
            this.lineMm = lineMm;
            this.circleMm = circleMm;
        }
    }

    private abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    }
}

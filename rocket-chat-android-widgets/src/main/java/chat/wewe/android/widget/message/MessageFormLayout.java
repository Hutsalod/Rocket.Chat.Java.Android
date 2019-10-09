package chat.wewe.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;

import chat.wewe.android.widget.R;

import static android.content.Context.MODE_PRIVATE;

public class MessageFormLayout extends LinearLayout {

  protected ViewGroup composer;

  private View btnExtra;
  private View btnSubmit;
  private MediaPlayer song;
  private LinearLayout layoutBlackList;
  private Button buttonBlackList;
  SharedPreferences SipData,SipDatas;
  private ExtraActionSelectionClickListener extraActionSelectionClickListener;
  private SubmitTextListener submitTextListener;
  private  BlocingUsers blocingUsers;
  private ImageKeyboardEditText.OnCommitContentListener listener;

  public MessageFormLayout(Context context) {
    super(context);
    init();

  }

  public MessageFormLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {

    composer = (ViewGroup) LayoutInflater.from(getContext())
        .inflate(R.layout.message_composer, this, false);
    song = (MediaPlayer) MediaPlayer.create(getContext(), R.raw.msg);
    btnExtra = composer.findViewById(R.id.btn_extras);
    layoutBlackList = composer.findViewById(R.id.layoutBlackList);

    btnExtra.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onExtraActionSelectionClick();
      }
    });

    btnSubmit = composer.findViewById(R.id.btn_submit);
    buttonBlackList = composer.findViewById(R.id.buttonBlackList);

    btnSubmit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String messageText = getText();
        if (messageText.length() > 0 && submitTextListener != null) {
          submitTextListener.onSubmitText(messageText);
          soungPlay(song);
          long date = System.currentTimeMillis();
          SimpleDateFormat datas = new SimpleDateFormat("H:m");
          String time = datas.format(date);
          SipData = getContext().getSharedPreferences("NameMessage", MODE_PRIVATE);
          SharedPreferences.Editor ed = SipData.edit();
          ed.putString(SipData.getString("getName", null), messageText);
          ed.commit();
          SipDatas = getContext().getSharedPreferences("TimeMessage", MODE_PRIVATE);
          SharedPreferences.Editor eds = SipDatas.edit();
          eds.commit();
          eds.putString(SipDatas.getString("getName", null), time);
          eds.commit();
        }
      }
    });


    buttonBlackList.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which){
              case DialogInterface.BUTTON_POSITIVE:
                blocingUsers.onSubmitText();
                layoutBlackList.setVisibility(VISIBLE);
                buttonBlackList.setVisibility(GONE);
                break;

              case DialogInterface.BUTTON_NEGATIVE:
                //No button clicked
                break;
            }
          }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Внимание").setMessage("Розблокировать пользователя?").setPositiveButton("Да", dialogClickListener)
                .setNegativeButton("Нет", dialogClickListener).show();

      }
        });

    buttonBlackList.setVisibility(GONE);

    btnSubmit.setScaleX(0);
    btnSubmit.setScaleY(0);
    btnSubmit.setVisibility(GONE);

    ImageKeyboardEditText editText = (ImageKeyboardEditText) composer.findViewById(R.id.editor);

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (TextUtils.getTrimmedLength(s) > 0) {
          animateShow(btnSubmit);
        } else {
          animateShow(btnExtra);
          animateHide(btnSubmit);
        }
      }
    });

    editText.setContentListener(new ImageKeyboardEditText.OnCommitContentListener() {
      @Override
      public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                     Bundle opts, String[] supportedMimeTypes) {
        if (listener != null) {
          return listener.onCommitContent(inputContentInfo, flags, opts, supportedMimeTypes);
        }
        return false;
      }
    });

    addView(composer);
  }

  public EditText getEditText() {
    return (EditText) composer.findViewById(R.id.editor);
  }

  public void setExtraActionSelectionClickListener(
      ExtraActionSelectionClickListener extraActionSelectionClickListener) {
    this.extraActionSelectionClickListener = extraActionSelectionClickListener;
  }

  public void setSubmitTextListener(SubmitTextListener submitTextListener) {
    this.submitTextListener = submitTextListener;
  }

  public void setBlocingUsers(BlocingUsers blocingUsers) {
    this.blocingUsers = blocingUsers;
  }



  private void onExtraActionSelectionClick() {
    if (extraActionSelectionClickListener != null) {
      extraActionSelectionClickListener.onClick();
    }
  }

  public void soungPlay(MediaPlayer sound){
    sound.start();
  }

  private EditText getEditor() {
    return (EditText) composer.findViewById(R.id.editor);
  }

  public final String getText() {
    return getEditor().getText().toString().trim();
  }

  public final void setText(final CharSequence text) {
    final EditText editor = getEditor();
    editor.post(new Runnable() {
      @Override
      public void run() {
        editor.setText(text);
        if (text.length() > 0) {
          editor.setSelection(text.length());
          InputMethodManager inputMethodManager = (InputMethodManager) editor.getContext()
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          editor.requestFocus();
          inputMethodManager.showSoftInput(editor, 0);
        }
      }
    });
  }

  public void setEnabled(boolean enabled) {
    getEditor().setEnabled(enabled);
    composer.findViewById(R.id.btn_submit).setEnabled(enabled);
  }

  public void setBlocing(boolean enabled) {

    Log.d("TEST23",""+enabled);
    if(enabled==true){
      buttonBlackList.setVisibility(VISIBLE);
    layoutBlackList.setVisibility(GONE);}
    else {
      layoutBlackList.setVisibility(VISIBLE);
      buttonBlackList.setVisibility(GONE);
    }
  }

  public void setEditTextCommitContentListener(
      ImageKeyboardEditText.OnCommitContentListener listener) {
    this.listener = listener;
  }

  private void animateHide(final View view) {
    view.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(GONE);
      }
    });
  }

  private void animateShow(final View view) {
    view.animate().scaleX(1).scaleY(1).setDuration(150).withStartAction(new Runnable() {
      @Override
      public void run() {
        view.setVisibility(VISIBLE);
      }
    });
  }

  public interface ExtraActionSelectionClickListener {
    void onClick();
  }

  public interface SubmitTextListener {
    void onSubmitText(String message);
  }

  public interface BlocingUsers {
    void onSubmitText();
  }


}

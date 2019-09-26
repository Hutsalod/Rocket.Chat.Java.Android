package chat.wewe.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;
import chat.wewe.android.widget.AbsoluteUrl;
import chat.wewe.android.widget.R;
import chat.wewe.android.widget.helper.FrescoHelper;
import chat.wewe.core.models.Attachment;
import chat.wewe.core.models.AttachmentAuthor;
import chat.wewe.core.models.AttachmentField;
import chat.wewe.core.models.AttachmentTitle;

/**
 */
public class RocketChatMessageAttachmentsLayout extends LinearLayout {
  private LayoutInflater inflater;
  private AbsoluteUrl absoluteUrl;
  private List<Attachment> attachments;

  public RocketChatMessageAttachmentsLayout(Context context) {
    super(context);
    initialize(context, null);
  }

  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                            int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    inflater = LayoutInflater.from(context);
    setOrientation(VERTICAL);
  }

  public void setAbsoluteUrl(AbsoluteUrl absoluteUrl) {
    this.absoluteUrl = absoluteUrl;
  }

  public void setAttachments(List<Attachment> attachments, boolean autoloadImages) {
    if (this.attachments != null && this.attachments.equals(attachments)) {
      return;
    }
    this.attachments = attachments;
    removeAllViews();

    for (int i = 0, size = attachments.size(); i < size; i++) {
      appendAttachmentView(attachments.get(i), autoloadImages);
    }
  }

  private void appendAttachmentView(Attachment attachment, boolean autoloadImages) {
    if (attachment == null) {
      return;
    }

    View attachmentView = inflater.inflate(R.layout.message_inline_attachment, this, false);

    colorizeAttachmentBar(attachment, attachmentView);
    showAuthorAttachment(attachment, attachmentView);
    showTitleAttachment(attachment, attachmentView);
    showReferenceAttachment(attachment, attachmentView);
    showImageAttachment(attachment, attachmentView, autoloadImages);
    // audio
    // video
    showFieldsAttachment(attachment, attachmentView);

    addView(attachmentView);
  }

  private void colorizeAttachmentBar(Attachment attachment, View attachmentView) {
    final View attachmentStrip = attachmentView.findViewById(R.id.attachment_strip);

    final String colorString = attachment.getColor();
    if (TextUtils.isEmpty(colorString)) {
      attachmentStrip.setBackgroundResource(R.color.inline_attachment_quote_line);
      return;
    }

    try {
      attachmentStrip.setBackgroundColor(Color.parseColor(colorString));
    } catch (Exception e) {
      attachmentStrip.setBackgroundResource(R.color.inline_attachment_quote_line);
    }
  }

  private void showAuthorAttachment(Attachment attachment, View attachmentView) {
    final View authorBox = attachmentView.findViewById(R.id.author_box);
    AttachmentAuthor author = attachment.getAttachmentAuthor();
    if (author == null) {
      authorBox.setVisibility(GONE);
      return;
    }

    authorBox.setVisibility(VISIBLE);

    FrescoHelper.setupDraweeAndLoadImage(absolutize(author.getIconUrl()),
        (SimpleDraweeView) attachmentView.findViewById(R.id.author_icon));

    final TextView authorName = (TextView) attachmentView.findViewById(R.id.author_name);
    authorName.setText(author.getName());

    final String link = absolutize(author.getLink());
    authorName.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        view.getContext().startActivity(intent);
      }
    });

    // timestamp and link - need to format time
  }

  private void showTitleAttachment(Attachment attachment, View attachmentView) {
    TextView titleView = (TextView) attachmentView.findViewById(R.id.title);
    AttachmentTitle title = attachment.getAttachmentTitle();
    if (title == null || title.getTitle() == null) {
      titleView.setVisibility(View.GONE);
      return;
    }

    titleView.setVisibility(View.VISIBLE);
    titleView.setText(title.getTitle());

    if (title.getLink() == null) {
      titleView.setOnClickListener(null);
      titleView.setClickable(false);
    } else {
      final String link = absolutize(title.getLink());
      titleView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

          final Context context = view.getContext();
          if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
          }
        }
      });
      TextViewCompat.setTextAppearance(titleView,
          R.style.TextAppearance_RocketChat_MessageAttachment_Title_Link);
    }
  }

  private void showReferenceAttachment(Attachment attachment, View attachmentView) {
    final View refBox = attachmentView.findViewById(R.id.ref_box);
    if (attachment.getThumbUrl() == null && attachment.getText() == null) {
      refBox.setVisibility(GONE);
      return;
    }

    refBox.setVisibility(VISIBLE);

    final SimpleDraweeView thumbImage = (SimpleDraweeView) refBox.findViewById(R.id.thumb);

    final String thumbUrl = attachment.getThumbUrl();
    if (TextUtils.isEmpty(thumbUrl)) {
      thumbImage.setVisibility(GONE);
    } else {
      thumbImage.setVisibility(VISIBLE);
      FrescoHelper.setupDraweeAndLoadImage(absolutize(thumbUrl), thumbImage);
    }

    final TextView refText = (TextView) refBox.findViewById(R.id.text);

    final String refString = attachment.getText();
    if (TextUtils.isEmpty(refString)) {
      refText.setVisibility(GONE);
    } else {
      refText.setVisibility(VISIBLE);
      refText.setText(refString);
    }
  }

  private void showImageAttachment(Attachment attachment, View attachmentView,
                                   boolean autoloadImages) {
    final View imageContainer = attachmentView.findViewById(R.id.image_container);
    if (attachment.getImageUrl() == null) {
      imageContainer.setVisibility(GONE);
      return;
    }

    imageContainer.setVisibility(VISIBLE);

    final SimpleDraweeView attachedImage =
        (SimpleDraweeView) attachmentView.findViewById(R.id.image);
    final View load = attachmentView.findViewById(R.id.image_load);

    loadImage(absolutize(attachment.getImageUrl()), attachedImage, load, autoloadImages);
  }

  private void showFieldsAttachment(Attachment attachment, View attachmentView) {
    List<AttachmentField> fields = attachment.getAttachmentFields();
    if (fields == null || fields.size() == 0) {
      return;
    }

    final ViewGroup attachmentContent =
        (ViewGroup) attachmentView.findViewById(R.id.attachment_content);

    for (int i = 0, size = fields.size(); i < size; i++) {
      final AttachmentField attachmentField = fields.get(i);
      if (attachmentField.getTitle() == null
          || attachmentField.getText() == null) {
        return;
      }
      MessageAttachmentFieldLayout fieldLayout = new MessageAttachmentFieldLayout(getContext());
      fieldLayout.setTitle(attachmentField.getTitle());
      fieldLayout.setValue(attachmentField.getText());

      attachmentContent.addView(fieldLayout);
    }
  }

  private String absolutize(String url) {
    if (absoluteUrl == null) {
      return url;
    }
    return absoluteUrl.from(url);
  }

  private void loadImage(final String url, final SimpleDraweeView drawee, final View load,
                         boolean autoloadImage) {
    if (autoloadImage) {
      load.setVisibility(GONE);
      FrescoHelper.setupDraweeAndLoadImage(url, drawee);
      return;
    }

    FrescoHelper.setupDrawee(drawee);
    load.setVisibility(GONE);
    load.setOnClickListener(null);
    FrescoHelper.loadImage(url, drawee);
    load.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        load.setOnClickListener(null);
        FrescoHelper.loadImage(url, drawee);
      }
    });
  }
}

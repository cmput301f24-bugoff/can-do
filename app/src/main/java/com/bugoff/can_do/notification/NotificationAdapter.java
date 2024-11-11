package com.bugoff.can_do.notification;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bugoff.can_do.R;

import java.util.List;

public class NotificationAdapter extends ArrayAdapter<Notification> {
    private Context context;
    private List<Notification> notifications;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        super(context, R.layout.notif_list_item, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.notif_list_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.notif_item_text);
        ImageButton moreVertButton = convertView.findViewById(R.id.notif_more_vert);

        Notification notification = notifications.get(position);
        textView.setText(notification.getContent());

        moreVertButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.notif_popup_layout, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            Button btnDelete = dialogView.findViewById(R.id.btn_delete);
            Button btnReport = dialogView.findViewById(R.id.btn_report);

            btnDelete.setOnClickListener(view -> {
                notifications.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Deleted: " + notification.getContent(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            btnReport.setOnClickListener(view -> {
                Toast.makeText(context, "Reported: " + notification.getContent(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            dialog.show();
        });

        return convertView;
    }
}

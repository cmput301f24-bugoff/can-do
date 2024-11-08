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

public class NotificationAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> notifications;

    public NotificationAdapter(Context context, List<String> notifications) {
        super(context, R.layout.notif_list_item, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.notif_list_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.notif_item_text);
        ImageButton moreVertButton = convertView.findViewById(R.id.notif_more_vert);

        // Set text for the item
        textView.setText(notifications.get(position));

        // Set OnClickListener for the button to show a dialog
        moreVertButton.setOnClickListener(v -> {
            // Create a dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.notif_popup_layout, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            // Set button click listeners using dialogView to avoid the "Cannot resolve symbol" error
            Button btnDelete = dialogView.findViewById(R.id.btn_delete);
            Button btnReport = dialogView.findViewById(R.id.btn_report);

            btnDelete.setOnClickListener(view -> {
                Toast.makeText(context, "Deleted: " + notifications.get(position), Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Dismiss the dialog after clicking
            });

            btnReport.setOnClickListener(view -> {
                Toast.makeText(context, "Reported: " + notifications.get(position), Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Dismiss the dialog after clicking
            });

            dialog.show(); // Show the dialog
        });

        return convertView;
    }
}

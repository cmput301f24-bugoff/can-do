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

/**
 * Custom ArrayAdapter for displaying notifications in a ListView.
 */
public class NotificationAdapter extends ArrayAdapter<Notification> {
    private Context context;
    private List<Notification> notifications;

    /**
     * Constructor for NotificationAdapter.
     *
     * @param context      the context in which the adapter is created
     * @param notifications the list of Notification objects to display
     */
    public NotificationAdapter(Context context, List<Notification> notifications) {
        super(context, R.layout.notif_list_item, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    /**
     * Provides a view for an AdapterView (ListView).
     *
     * @param position    the position of the item within the adapter's data set
     * @param convertView the old view to reuse, if possible
     * @param parent      the parent that this view will eventually be attached to
     * @return the View for the corresponding position in the adapter
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.notif_list_item, parent, false);
        }

        // Set the content of the notification to the TextView
        TextView textView = convertView.findViewById(R.id.notif_item_text);
        ImageButton moreVertButton = convertView.findViewById(R.id.notif_more_vert);
        Notification notification = notifications.get(position);
        textView.setText(notification.getContent());

        // Set OnClickListener for the options button to display a dialog
        moreVertButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.notif_popup_layout, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            // Set button click listeners
            Button btnDelete = dialogView.findViewById(R.id.btn_delete);
            Button btnReport = dialogView.findViewById(R.id.btn_report);

            btnDelete.setOnClickListener(view -> {
                notifications.remove(position); // Remove the notification from the list
                notifyDataSetChanged(); // Refresh the adapter to reflect the change
                Toast.makeText(context, "Deleted: " + notification.getContent(), Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Close the dialog
            });

            btnReport.setOnClickListener(view -> {
                Toast.makeText(context, "Reported: " + notification.getContent(), Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Close the dialog
            });

            dialog.show(); // Display the dialog
        });

        return convertView;
    }
}

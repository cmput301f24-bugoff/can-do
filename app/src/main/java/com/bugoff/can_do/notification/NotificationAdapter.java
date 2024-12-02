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
 * Adapter class for displaying notifications in a ListView.
 * Handles the population of notification data into custom list items
 * and provides functionality for managing notifications through a popup dialog.
 */
public class NotificationAdapter extends ArrayAdapter<Notification> {

    /**
     * Context of the application environment.
     */
    private Context context;

    /**
     * List of notifications to display in the adapter.
     */
    private List<Notification> notifications;

    /**
     * Constructs a new NotificationAdapter.
     *
     * @param context      The context of the current environment.
     * @param notifications The list of notifications to display.
     */
    public NotificationAdapter(Context context, List<Notification> notifications) {
        super(context, R.layout.notif_list_item, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    /**
     * Populates a list item view with notification data.
     *
     * @param position    The position of the item in the list.
     * @param convertView The recycled view to populate, or null if a new view needs to be created.
     * @param parent      The parent ViewGroup containing the list item.
     * @return The populated view for the list item.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.notif_list_item, parent, false);
        }

        // Initialize views in the list item layout.
        TextView textView = convertView.findViewById(R.id.notif_item_text);
        ImageButton moreVertButton = convertView.findViewById(R.id.notif_more_vert);

        // Get the notification for the current position.
        Notification notification = notifications.get(position);
        textView.setText(notification.getContent());

        // Set up the "More" button to show a popup dialog for actions.
        moreVertButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.notif_popup_layout, null);
            builder.setView(dialogView);

            // Create and configure the dialog.
            AlertDialog dialog = builder.create();

            // Initialize buttons in the popup dialog.
            Button btnDelete = dialogView.findViewById(R.id.btn_delete);
            Button btnReport = dialogView.findViewById(R.id.btn_report);

            // Set up the "Delete" button functionality.
            btnDelete.setOnClickListener(view -> {
                notifications.remove(position);
                notifyDataSetChanged(); // Notify the adapter of data changes.
                Toast.makeText(context, "Deleted: " + notification.getContent(), Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Close the dialog.
            });

            // Set up the "Report" button functionality.
            btnReport.setOnClickListener(view -> {
                Toast.makeText(context, "Reported: " + notification.getContent(), Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Close the dialog.
            });

            dialog.show(); // Display the dialog.
        });

        return convertView; // Return the populated view.
    }
}

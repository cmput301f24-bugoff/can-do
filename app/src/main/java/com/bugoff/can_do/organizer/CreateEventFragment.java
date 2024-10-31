package com.bugoff.can_do.organizer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugoff.can_do.R;
import com.bugoff.can_do.database.GlobalRepository;
import com.bugoff.can_do.event.Event;
import com.bugoff.can_do.facility.Facility;
import com.bugoff.can_do.user.User;

import org.jetbrains.annotations.Contract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateEventFragment extends Fragment {
    private static final String TAG = "CreateEventFragment";
    private EditText editTextEventName;
    private EditText editTextEventDescription;
    private Button buttonRegStartDate;
    private Button buttonRegEndDate;
    private Button buttonEventStartDate;
    private Button buttonEventEndDate;
    private EditText editTextNumParticipants;
    private CheckBox checkBoxGeolocation;
    private Button buttonCreateEvent;

    private Date registrationStartDate;
    private Date registrationEndDate;
    private Date eventStartDate;
    private Date eventEndDate;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public CreateEventFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Contract(" -> new")
    public static CreateEventFragment newInstance() {
        return new CreateEventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupDatePickers();
        setupCreateEventButton();
    }

    private void initializeViews(@NonNull View view) {
        editTextEventName = view.findViewById(R.id.editTextEventName);
        editTextEventDescription = view.findViewById(R.id.editTextEventDescription);
        buttonRegStartDate = view.findViewById(R.id.buttonRegStartDate);
        buttonRegEndDate = view.findViewById(R.id.buttonRegEndDate);
        buttonEventStartDate = view.findViewById(R.id.buttonEventStartDate);
        buttonEventEndDate = view.findViewById(R.id.buttonEventEndDate);
        editTextNumParticipants = view.findViewById(R.id.editTextNumParticipants);
        checkBoxGeolocation = view.findViewById(R.id.checkBoxGeolocation);
        buttonCreateEvent = view.findViewById(R.id.buttonCreateEvent);
    }

    private void setupDatePickers() {
        buttonRegStartDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            registrationStartDate = date;
            buttonRegStartDate.setText(dateFormat.format(date));
        }));

        buttonRegEndDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            registrationEndDate = date;
            buttonRegEndDate.setText(dateFormat.format(date));
        }));

        buttonEventStartDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            eventStartDate = date;
            buttonEventStartDate.setText(dateFormat.format(date));
        }));

        buttonEventEndDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            eventEndDate = date;
            buttonEventEndDate.setText(dateFormat.format(date));
        }));
    }

    private void showDatePickerDialog(DateSelectedListener listener) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    listener.onDateSelected(calendar.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupCreateEventButton() {
        buttonCreateEvent.setOnClickListener(v -> createEvent());
    }

    private void createEvent() {
        String eventName = editTextEventName.getText().toString().trim();
        String eventDescription = editTextEventDescription.getText().toString().trim();
        String numParticipantsStr = editTextNumParticipants.getText().toString().trim();
        boolean isGeolocationRequired = checkBoxGeolocation.isChecked();

        // Validate inputs
        if (TextUtils.isEmpty(eventName)) {
            editTextEventName.setError("Event name is required");
            return;
        }

        if (TextUtils.isEmpty(eventDescription)) {
            editTextEventDescription.setError("Event description is required");
            return;
        }

        if (registrationStartDate == null) {
            Toast.makeText(getContext(), "Please select Registration Start Date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (registrationEndDate == null) {
            Toast.makeText(getContext(), "Please select Registration End Date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventStartDate == null) {
            Toast.makeText(getContext(), "Please select Event Start Date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventEndDate == null) {
            Toast.makeText(getContext(), "Please select Event End Date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (registrationEndDate.before(registrationStartDate)) {
            Toast.makeText(getContext(), "Registration End Date cannot be before Start Date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventEndDate.before(eventStartDate)) {
            Toast.makeText(getContext(), "Event End Date cannot be before Start Date", Toast.LENGTH_SHORT).show();
            return;
        }

        int numParticipants;
        try {
            numParticipants = Integer.parseInt(numParticipantsStr);
            if (numParticipants <= 0) {
                editTextNumParticipants.setError("Number of participants must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            editTextNumParticipants.setError("Invalid number");
            return;
        }

        // Retrieve the current user and their facility
        User currentUser = GlobalRepository.getLoggedInUser();
        Log.d(TAG, "createEvent: " + currentUser);
        Log.d(TAG, "createEvent: " + currentUser.getFacility());
        if (currentUser == null || currentUser.getFacility() == null) {
            Toast.makeText(getContext(), "User or Facility not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Facility facility = currentUser.getFacility();

        // Create a new Event instance
        Event newEvent = new Event(facility);
        newEvent.setName(eventName);
        newEvent.setDescription(eventDescription);
        newEvent.setRegistrationStartDate(registrationStartDate);
        newEvent.setRegistrationEndDate(registrationEndDate);
        newEvent.setEventStartDate(eventStartDate);
        newEvent.setEventEndDate(eventEndDate);
        newEvent.setNumberOfParticipants(numParticipants);
        newEvent.setGeolocationRequired(isGeolocationRequired);

        // Save the Event to Firestore using GlobalRepository
        GlobalRepository.addEvent(newEvent).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                navigateToOrganizerMain();
                // TODO: Navigate to inside the event
            } else {
                Toast.makeText(getContext(), "Failed to create event: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CreateEventFragment", "Error creating event", task.getException());
            }
        });
    }

    private void navigateToOrganizerMain() {
        Intent intent = new Intent(getActivity(), OrganizerMain.class);
        startActivity(intent);
        requireActivity().finish(); // Optional: Finish the current Activity if you don't want to return to it
    }

    // Listener interface for date selection
    private interface DateSelectedListener {
        void onDateSelected(Date date);
    }
}

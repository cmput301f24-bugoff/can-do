package com.bugoff.can_do.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import java.util.Objects;

public class CreateEventFragment extends Fragment {
    private static final String TAG = "CreateEventFragment";

    private EditText editTextEventName;
    private EditText editTextEventDescription;
    private Button buttonRegStartDate;
    private Button buttonRegStartTime;
    private Button buttonRegEndDate;
    private Button buttonRegEndTime;
    private Button buttonEventStartDate;
    private Button buttonEventStartTime;
    private Button buttonEventEndDate;
    private Button buttonEventEndTime;
    private EditText editTextNumParticipants;
    private CheckBox checkBoxGeolocation;
    private Button buttonCreateEvent;

    private Date registrationStartDate;
    private Date registrationEndDate;
    private Date eventStartDate;
    private Date eventEndDate;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

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
        setupDateTimePickers();
        setupCreateEventButton();
    }

    private void initializeViews(@NonNull View view) {
        editTextEventName = view.findViewById(R.id.editTextEventName);
        editTextEventDescription = view.findViewById(R.id.editTextEventDescription);
        buttonRegStartDate = view.findViewById(R.id.buttonRegStartDate);
        buttonRegStartTime = view.findViewById(R.id.buttonRegStartTime);
        buttonRegEndDate = view.findViewById(R.id.buttonRegEndDate);
        buttonRegEndTime = view.findViewById(R.id.buttonRegEndTime);
        buttonEventStartDate = view.findViewById(R.id.buttonEventStartDate);
        buttonEventStartTime = view.findViewById(R.id.buttonEventStartTime);
        buttonEventEndDate = view.findViewById(R.id.buttonEventEndDate);
        buttonEventEndTime = view.findViewById(R.id.buttonEventEndTime);
        editTextNumParticipants = view.findViewById(R.id.editTextMaxNumParticipants);
        checkBoxGeolocation = view.findViewById(R.id.checkBoxGeolocation);
        buttonCreateEvent = view.findViewById(R.id.buttonCreateEvent);
    }

    private void setupDateTimePickers() {
        // Registration Start Date and Time
        buttonRegStartDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            registrationStartDate = setDate(registrationStartDate, date);
            updateButtonText(buttonRegStartDate, registrationStartDate);
        }));

        buttonRegStartTime.setOnClickListener(v -> showTimePickerDialog(time -> {
            registrationStartDate = setTime(registrationStartDate, time);
            updateButtonText(buttonRegStartTime, registrationStartDate);
        }));

        // Registration End Date and Time
        buttonRegEndDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            registrationEndDate = setDate(registrationEndDate, date);
            updateButtonText(buttonRegEndDate, registrationEndDate);
        }));

        buttonRegEndTime.setOnClickListener(v -> showTimePickerDialog(time -> {
            registrationEndDate = setTime(registrationEndDate, time);
            updateButtonText(buttonRegEndTime, registrationEndDate);
        }));

        // Event Start Date and Time
        buttonEventStartDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            eventStartDate = setDate(eventStartDate, date);
            updateButtonText(buttonEventStartDate, eventStartDate);
        }));

        buttonEventStartTime.setOnClickListener(v -> showTimePickerDialog(time -> {
            eventStartDate = setTime(eventStartDate, time);
            updateButtonText(buttonEventStartTime, eventStartDate);
        }));

        // Event End Date and Time
        buttonEventEndDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            eventEndDate = setDate(eventEndDate, date);
            updateButtonText(buttonEventEndDate, eventEndDate);
        }));

        buttonEventEndTime.setOnClickListener(v -> showTimePickerDialog(time -> {
            eventEndDate = setTime(eventEndDate, time);
            updateButtonText(buttonEventEndTime, eventEndDate);
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

    /**
     * Displays a TimePickerDialog and returns the selected time.
     */
    private void showTimePickerDialog(TimeSelectedListener listener) {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    listener.onTimeSelected(calendar.getTime());
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void updateButtonText(Button button, Date date) {
        if (button == buttonRegStartDate || button == buttonRegEndDate ||
                button == buttonEventStartDate || button == buttonEventEndDate) {
            button.setText(dateFormat.format(date));
        } else {
            button.setText(timeFormat.format(date));
        }
    }

    private Date setDate(Date original, Date selectedDate) {
        Calendar originalCal = Calendar.getInstance();
        if (original != null) {
            originalCal.setTime(original);
        } else {
            originalCal.setTime(new Date());
        }

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);

        originalCal.set(Calendar.YEAR, selectedCal.get(Calendar.YEAR));
        originalCal.set(Calendar.MONTH, selectedCal.get(Calendar.MONTH));
        originalCal.set(Calendar.DAY_OF_MONTH, selectedCal.get(Calendar.DAY_OF_MONTH));

        return originalCal.getTime();
    }

    /**
     * Sets the time part of the Date object while retaining the date.
     */
    private Date setTime(Date original, Date selectedTime) {
        Calendar originalCal = Calendar.getInstance();
        if (original != null) {
            originalCal.setTime(original);
        } else {
            originalCal.setTime(new Date());
        }

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedTime);

        originalCal.set(Calendar.HOUR_OF_DAY, selectedCal.get(Calendar.HOUR_OF_DAY));
        originalCal.set(Calendar.MINUTE, selectedCal.get(Calendar.MINUTE));
        originalCal.set(Calendar.SECOND, 0);
        originalCal.set(Calendar.MILLISECOND, 0);

        return originalCal.getTime();
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
            Toast.makeText(getContext(), "Please select Registration Start Date and Time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (registrationEndDate == null) {
            Toast.makeText(getContext(), "Please select Registration End Date and Time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventStartDate == null) {
            Toast.makeText(getContext(), "Please select Event Start Date and Time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventEndDate == null) {
            Toast.makeText(getContext(), "Please select Event End Date and Time", Toast.LENGTH_SHORT).show();
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

        int maxNumParticipants;
        try {
            maxNumParticipants = Integer.parseInt(numParticipantsStr);
            if (maxNumParticipants <= 0) {
                editTextNumParticipants.setError("Max number of participants must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            editTextNumParticipants.setError("Invalid number");
            return;
        }

        // Retrieve the current user and their facility
        User currentUser = GlobalRepository.getLoggedInUser();
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
        newEvent.setMaxNumberOfParticipants(maxNumParticipants);
        newEvent.setGeolocationRequired(isGeolocationRequired);

        // Save the Event to Firestore using GlobalRepository
        GlobalRepository.addEvent(newEvent).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                navigateToOrganizerMain(); // TODO: maybe navigate to inside the event instead
            } else {
                Toast.makeText(getContext(), "Failed to create event: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                Log.e("CreateEventFragment", "Error creating event", task.getException());
            }
        });
    }

    private void navigateToOrganizerMain() {
        Intent intent = new Intent(getActivity(), OrganizerMain.class);
        startActivity(intent);
    }

    // Listener interfaces for date and time selection
    private interface DateSelectedListener {
        void onDateSelected(Date date);
    }

    private interface TimeSelectedListener {
        void onTimeSelected(Date time);
    }
}

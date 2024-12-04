package com.bugoff.can_do.testclasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaitlistDrawer {
    private List<TestUser> waitlist;
    private List<TestUser> selectedUsers;
    private final Random random;

    public WaitlistDrawer() {
        this.waitlist = new ArrayList<>();
        this.selectedUsers = new ArrayList<>();
        this.random = new Random();
    }

    public void addToWaitlist(TestUser user) {
        waitlist.add(user);
    }

    public List<TestUser> performDrawing(int numberToDraw) {
        if (numberToDraw <= 0) {
            throw new IllegalArgumentException("Number to draw must be positive");
        }

        if (numberToDraw > waitlist.size()) {
            throw new IllegalArgumentException("Cannot draw more users than are in waitlist");
        }

        List<TestUser> newlySelected = new ArrayList<>();
        List<TestUser> remainingUsers = new ArrayList<>(waitlist);

        for (int i = 0; i < numberToDraw && !remainingUsers.isEmpty(); i++) {
            int randomIndex = random.nextInt(remainingUsers.size());
            TestUser selectedUser = remainingUsers.get(randomIndex);
            newlySelected.add(selectedUser);
            remainingUsers.remove(randomIndex);
        }

        // Update internal state
        selectedUsers.addAll(newlySelected);
        waitlist = remainingUsers;

        return newlySelected;
    }

    public List<TestUser> getWaitlist() {
        return new ArrayList<>(waitlist);
    }

    public List<TestUser> getSelectedUsers() {
        return new ArrayList<>(selectedUsers);
    }
}

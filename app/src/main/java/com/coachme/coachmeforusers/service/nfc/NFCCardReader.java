package com.coachme.coachmeforusers.service.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.widget.Toast;

import com.coachme.coachmeforusers.activities.reservations.ReservationTimePickerActivity;
import com.coachme.coachmeforusers.model.User;
import com.coachme.coachmeforusers.utils.Helper;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.IOException;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.coachme.coachmeforusers.CoachMeForUsersApp.getContext;
import static com.coachme.coachmeforusers.CoachMeForUsersApp.getCurrentUser;
import static com.coachme.coachmeforusers.CoachMeForUsersApp.setCurrentUser;
import static com.coachme.coachmeforusers.utils.Helper.API_ENDPOINT;

public class NFCCardReader implements NfcAdapter.ReaderCallback {

    @Override
    public void onTagDiscovered(Tag tag) {
        readTag(tag);
    }

    public void readTag(Tag tag) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        if (mifare != null) {
            try {
                User user = getCurrentUser();
                if (!user.isLoggedOnAMachine()) {
                    user.setLoggedOnAMachine(true);
                    String jsonUser = Helper.convertObjectToJson(user);
                    ClientResource userResource = new ClientResource(API_ENDPOINT + "/users/" + user.getId());
                    userResource.put(new JsonRepresentation(jsonUser), MediaType.APPLICATION_JSON);

                    setCurrentUser(user);

                    Intent intent = new Intent(getContext(), ReservationTimePickerActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);

                    try {
                        mifare.close();
                    } catch (IOException e) {
                        Toast.makeText(getContext(),
                                "Une erreur est survenue lors du chargement des données utilsateurs.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Vous ne pouvez pas vous connecter sur plusieurs machines à la fois. Veuillez-vous déconnecter de la précédente machine sur laquelle vous étiez.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            } catch (ResourceException e) {
                if (e.getStatus() == Status.CLIENT_ERROR_NOT_FOUND) {
                    Toast.makeText(getContext(),
                            "Aucun adhérent n'est associé avec l'identifiant saisi.",
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getContext(),
                            "Une erreur est survenue lors la connexion utilisateur sur la machine.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }
}

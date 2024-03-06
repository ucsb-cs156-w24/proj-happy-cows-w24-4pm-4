import { render, waitFor, fireEvent, screen } from "@testing-library/react";
import AnnouncementsForm from "main/components/Announcements/announcementsForm";
import { announcementsFixtures } from "fixtures/announcementsFixtures"
import { BrowserRouter as Router } from "react-router-dom";

const mockedNavigate = jest.fn();

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockedNavigate
}));


describe("Announcements tests", () => {

    test("renders correctly", async () => {

        render(
            <Router  >
                <AnnouncementsForm />
            </Router>
        );
        await screen.findByText(/Start \(iso format\)/);
        await screen.findByText(/End \(iso format\)/);
        await screen.findByText(/Announcement/);
        await screen.findByText(/Create/);
    });


    test("renders correctly when passing in an Announcement", async () => {

        render(
            <Router  >
                <AnnouncementsForm initialContents={announcementsFixtures.oneAnnouncement} />
            </Router>
        );
        await screen.findByTestId(/announcementsForm-id/);
        expect(screen.getByText(/Id/)).toBeInTheDocument();
        expect(screen.getByTestId(/announcementsForm-id/)).toHaveValue("1");
    });


    test("Correct Error messsages on bad input", async () => {

        render(
            <Router  >
                <AnnouncementsForm />
            </Router>
        );
        await screen.findByTestId("announcementsForm-start");
        const startField = screen.getByTestId("announcementsForm-start");
        const endField = screen.getByTestId("announcementsForm-end");
        const announcementField = screen.getByTestId("announcementsForm-announcement");
        const submitButton = screen.getByTestId("announcementsForm-submit");

        fireEvent.change(startField, { target: { value: 'bad-input' } });
        fireEvent.change(endField, { target: { value: 'bad-input' } });
        fireEvent.change(announcementField, { target: { value: 'bad-input' } });
        fireEvent.click(submitButton);

        await screen.findByText(/Start is required./);
        //expect(screen.getByText(/Start must be provided in ISO format./)).toBeInTheDocument();
        //expect(screen.getByText(/End must be provided in ISO format./)).toBeInTheDocument();
    });

    test("Correct Error messsages on missing input", async () => {

        render(
            <Router  >
                <AnnouncementsForm />
            </Router>
        );
        await screen.findByTestId("announcementsForm-submit");
        const submitButton = screen.getByTestId("announcementsForm-submit");

        fireEvent.click(submitButton);

        await screen.findByText(/Start is required./);
        expect(screen.getByText(/Announcement is required./)).toBeInTheDocument();

    });

    test("No Error messsages on good input", async () => {

        const mockSubmitAction = jest.fn();


        render(
            <Router  >
                <AnnouncementsForm submitAction={mockSubmitAction} />
            </Router>
        );
        await screen.findByTestId("announcementsForm-start");

        const startField = screen.getByTestId("announcementsForm-start");
        const endField = screen.getByTestId("announcementsForm-end");
        const announcementField = screen.getByTestId("announcementsForm-announcement");
        const submitButton = screen.getByTestId("announcementsForm-submit");

        fireEvent.change(startField, { target: { value: '2022-01-02T12:00' } });
        fireEvent.change(endField, { target: { value: '2022-01-22T12:01:00.00' } });
        fireEvent.change(announcementField, { target: { value: 'Hello World' } });
        fireEvent.click(submitButton);

        await waitFor(() => expect(mockSubmitAction).toHaveBeenCalled());

        expect(screen.queryByText(/Start is required. /)).not.toBeInTheDocument();
        //expect(screen.queryByText(/Start must be provided in ISO format./)).not.toBeInTheDocument();
        //expect(screen.queryByText(/End must be provided in ISO format./)).not.toBeInTheDocument();

    });


    test("that navigate(-1) is called when Cancel is clicked", async () => {

        render(
            <Router  >
                <AnnouncementsForm />
            </Router>
        );
        await screen.findByTestId("announcementsForm-cancel");
        const cancelButton = screen.getByTestId("announcementsForm-cancel");

        fireEvent.click(cancelButton);

        await waitFor(() => expect(mockedNavigate).toHaveBeenCalledWith(-1));

    });

});

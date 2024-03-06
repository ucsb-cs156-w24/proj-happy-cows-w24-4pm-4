import { Button, Form, Row, Col } from 'react-bootstrap';
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'

function announcementsForm({ initialContents, submitAction, buttonLabel = "Create" }) {

    // Stryker disable all
    const {
        register,
        formState: { errors },
        handleSubmit,
    } = useForm(
        { defaultValues: initialContents || {}, }
    );
    // Stryker restore all

    const navigate = useNavigate();

    // For explanation, see: https://stackoverflow.com/questions/3143070/javascript-regex-iso-datetime
    // Note that even this complex regex may still need some tweaks

    // Stryker disable next-line Regex
    const isodate_regex = /(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+)|(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d)|(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d)/i;

    return (

        <Form onSubmit={handleSubmit(submitAction)}>


            <Row>
                {initialContents && (
                    <Col>
                        <Form.Group className="mb-3" >
                            <Form.Label htmlFor="id">Id</Form.Label>
                            <Form.Control
                                data-testid="announcementsForm-id"
                                id="id"
                                type="text"
                                {...register("id")}
                                value={initialContents.id}
                                disabled
                            />
                        </Form.Group>
                    </Col>
                )}

                <Col>
                    <Form.Group className="mb-3" >
                        <Form.Label htmlFor="start">Start (iso format)</Form.Label>
                        <Form.Control
                            data-testid="announcementsForm-start"
                            id="start"
                            type="datetime-local"
                            isInvalid={Boolean(errors.start)}
                            {...register("start", { required: true, pattern: { value: isodate_regex, message: 'Start must be provided in ISO format.' }})}
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.start && 'Start is required. '}

                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
                <Col>
                    <Form.Group className="mb-3" >
                        <Form.Label htmlFor="end">End (iso format)</Form.Label>
                        <Form.Control
                            data-testid="announcementsForm-end"
                            id="end"
                            type="datetime-local"
                            isInvalid={Boolean(errors.end)}
                            {...register("end", { pattern: { value: isodate_regex, message: 'End must be provided in ISO format.'} })}
                        />
                        <Form.Control.Feedback type="invalid">
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
            </Row>

            <Row>
                <Col>
                    <Form.Group className="mb-3" >
                        <Form.Label htmlFor="announcement">Announcement</Form.Label>
                        <Form.Control
                            data-testid="announcementsForm-announcement"
                            id="announcement"
                            type="text"
                            isInvalid={Boolean(errors.announcement)}
                            {...register("announcement", {
                                required: "Announcement is required."
                            })}
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.announcement?.message}
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
            </Row>

            <Row>
                <Col>
                    <Button
                        type="submit"
                        data-testid="announcementsForm-submit"
                    >
                        {buttonLabel}
                    </Button>
                    <Button
                        variant="Secondary"
                        onClick={() => navigate(-1)}
                        data-testid="announcementsForm-cancel"
                    >
                        Cancel
                    </Button>
                </Col>
            </Row>
        </Form>

    )
}

export default announcementsForm;

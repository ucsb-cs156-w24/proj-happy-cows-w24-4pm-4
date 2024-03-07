import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import AnnouncementsForm from "main/components/Announcements/AnnouncementsForm";
import { Navigate } from 'react-router-dom'
import { useBackendMutation } from "main/utils/useBackend";
import { toast } from "react-toastify"

export default function AnnouncementsCreatePage({storybook=false}) {

  const objectToAxiosParams = (announcement) => ({
    url: "/api/announcements/post",
    method: "POST",
    params: {
      id: announcement.id,
      commonsId: announcement.commonsId,
      start: announcement.start,
      end: announcement.end,
      announcement: announcement.announcement
    }
  });

  const onSuccess = (announcement) => {
    toast(`New Announcement Created - id: ${announcement.id}, commonsId: ${announcement.commonsId}`);
  }

  const mutation = useBackendMutation(
    objectToAxiosParams,
     { onSuccess },
     // Stryker disable next-line all : hard to set up test for caching
     ["/api/announcements/all"]
     );

  const { isSuccess } = mutation

  const onSubmit = async (data) => {
    mutation.mutate(data);
  }

  if (isSuccess && !storybook) {
    return <Navigate to="/announcements" />
  }


  // Stryker disable all : placeholder for future implementation
  return (
    <BasicLayout>
      <div className="pt-2">
        <h1>Create New Announcement for Commons</h1>

        <AnnouncementsForm submitAction={onSubmit} />

      </div>
    </BasicLayout>
  )
}

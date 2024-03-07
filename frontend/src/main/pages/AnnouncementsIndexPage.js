import React from 'react'
import { useBackend } from 'main/utils/useBackend';

import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import AnnouncementsTable from 'main/components/Announcements/AnnouncementsTable';
import { Button } from 'react-bootstrap';
import { useCurrentUser , hasRole} from 'main/utils/currentUser';


export default function AnnouncementsIndexPage() {

  const currentUser = useCurrentUser();
  //const commonsId = ;
  //const commonsName =;

  const createButton = () => {
    if (hasRole(currentUser, "ROLE_ADMIN")) {
        return (
            <Button
                variant="primary"
                href="/admin/announcements/:commonsId/create"
                style={{ float: "right" }}
            >
                Create Announcement
            </Button>
        )
    }
  }

  const { data: announcements, error: _error, status: _status } =
    useBackend(
      // Stryker disable next-line all : don't test internal caching of React Query
      [`api/announcements/:commonsId/all`],
      { method: "GET", url: `/api/announcements/:commonsId/all` },
      // Stryker disable next-line all : don't test default value of empty list
      []
    );

  return (
    <BasicLayout>
      <div className="pt-2">
        {createButton()}
        <h1>Announcements for </h1>
        <AnnouncementsTable announcements={announcements} currentUser={currentUser} />
      </div>
    </BasicLayout>
  )
}

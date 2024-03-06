import React from 'react';
import { announcementsFixtures } from 'fixtures/announcementsFixtures'
import { currentUserFixtures } from 'fixtures/currentUserFixtures';
import { rest } from "msw";
import AnnouncementsTable from 'main/components/Announcements/AnnouncementsTable';

export default {
    title: 'components/Announcements/AnnouncementsTable',
    component: AnnouncementsTable
};

const Template = (args) => {
    return (
        <AnnouncementsTable {...args} />
    )
};

export const Empty = Template.bind({});

Empty.args = {
    announcements: []
};

export const ThreeItemsOrdinaryUser = Template.bind({});

ThreeItemsOrdinaryUser.args = {
    announcemenets: announcementsFixtures.threeAnnouncements,
    currentUser: currentUserFixtures.userOnly,
};

export const ThreeItemsAdminUser = Template.bind({});
ThreeItemsAdminUser.args = {
    announcements: announcementsFixtures.threeAnnouncements,
    currentUser: currentUserFixtures.adminUser,
}

ThreeItemsAdminUser.parameters = {
    msw: [
        rest.delete('/api/announcements', (req, res, ctx) => {
            window.alert("DELETE: " + JSON.stringify(req.url));
            return res(ctx.status(200),ctx.json({}));
        }),
    ]
};

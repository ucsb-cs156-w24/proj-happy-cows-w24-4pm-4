import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import OurTable, { ButtonColumn, DateColumn, PlaintextColumn} from "main/components/OurTable";

describe("OurTable tests", () => {
    const data = [
        {
            col1: 'Hello',
            col2: 'asd',
            createdAt: '2021-04-01T04:00:00.000',
            log: "foo\nbar\n  baz",
        },
        {
            col1: 'react-table',
            col2: 'hdhdh',
            createdAt: '2022-01-04T14:00:00.000',
            log: "foo\nbar",

        },
        {
            col1: 'whatever',
            col2: 'jiji',
            createdAt: '2023-04-01T23:00:00.000',
            log: "bar\n  baz",
        },
        {
            col1: '3',
            col2: '4',
            createdAt: '2023-04-01T23:00:00.000',
            log: "5",
        },
        {
            col1: '4',
            col2: '5',
            createdAt: '2023-04-01T23:00:00.000',
            log: "6",
        },
        {
            col1: '5',
            col2: '6',
            createdAt: '2023-04-01T23:00:00.000',
            log: "7",
        },
        {
            col1: '6',
            col2: '7',
            createdAt: '2023-04-01T23:00:00.000',
            log: "8",
        },
        {
            col1: '7',
            col2: '8',
            createdAt: '2023-04-01T23:00:00.000',
            log: "9",
        },
        {
            col1: '8',
            col2: '9',
            createdAt: '2023-04-01T23:00:00.000',
            log: "10",
        },
        {
            col1: '9',
            col2: '10',
            createdAt: '2023-04-01T23:00:00.000',
            log: "11",
        },
        {
            col1: '10',
            col2: '11',
            createdAt: '2023-04-01T23:00:00.000',
            log: "12",
        }
    ];

    const clickMeCallback = jest.fn();

    const columns = [
        {
            Header: 'Column 1',
            accessor: 'col1', // accessor is the "key" in the data
        },
        {
            Header: 'Column 2',
            accessor: 'col2',
        },
        ButtonColumn("Click", "primary", clickMeCallback, "testId"),
        DateColumn("Date", (cell) => cell.row.original.createdAt),
        PlaintextColumn("Log", (cell) => cell.row.original.log),
    ];

    test("renders an empty table without crashing", () => {
        render(
            <OurTable columns={columns} data={[]} />
        );
    });

    test("renders a table with two rows without crashing", () => {
        render(
            <OurTable columns={columns} data={data.slice(0,2)} />
        );
    });

    test("The button appears in the table", async () => {
        render(
            <OurTable columns={columns} data={data} />
        );

        expect(await screen.findByTestId("testId-cell-row-0-col-Click-button")).toBeInTheDocument();
        const button = screen.getByTestId("testId-cell-row-0-col-Click-button");
        fireEvent.click(button);
        await waitFor(() => expect(clickMeCallback).toBeCalledTimes(1));
    });

    test("default testid is testId", async () => {
        render(
            <OurTable columns={columns} data={data} />
        );
        expect(await screen.findByTestId("testid-header-col1")).toBeInTheDocument();
    });

    test("click on a header and a sort caret should appear", async () => {
        render(
            <OurTable columns={columns} data={data} testid={"sampleTestId"} />
        );

        expect(await screen.findByTestId("sampleTestId-header-col1")).toBeInTheDocument();
        const col1Header = screen.getByTestId("sampleTestId-header-col1");

        const col1SortCarets = screen.getByTestId("sampleTestId-header-col1-sort-carets");
        expect(col1SortCarets).toHaveTextContent('');

        const col1Row0 = screen.getByTestId("sampleTestId-cell-row-0-col-col1");
        expect(col1Row0).toHaveTextContent("Hello");

        fireEvent.click(col1Header);
        expect(await screen.findByText("🔼")).toBeInTheDocument();

        fireEvent.click(col1Header);
        expect(await screen.findByText("🔽")).toBeInTheDocument();
    });

    test("pagination ui should not be visible when the number of rows of data is less than or equal to the default page size (10)", async () => {
        render(
            <OurTable columns={columns} data={data.slice(0,10)} testid={"sampleTestId"}/>
        );
        expect(screen.getByTestId('sampleTestId-cell-row-9-col-col1')).toBeInTheDocument(); //confirm the last row is in the table of 10 rows
        expect(screen.queryByTestId("pagination-ui")).not.toBeInTheDocument(); 
    });

    test("pagination ui should be visible when the number of rows of data is more than the default page size (10)", async () => {
        render(
            <OurTable columns={columns} data={data}/>
        );
        expect(screen.getByTestId("pagination-ui")).toBeInTheDocument(); 
    });

    test("correct data is rendered when next page or previous page buttons is clicked", async () => {
        render(
            <OurTable columns={columns} data={data} testid={"sampleTestId"}/>
        );
        expect(await screen.findByTestId("page-indicator")).toHaveTextContent("Page 1 of 2"); // confirm current page number
        expect(screen.getByTestId('sampleTestId-cell-row-9-col-col1')).toBeInTheDocument();
        fireEvent.click(screen.getByTestId("goto-next-page-button"));
        expect(await screen.findByTestId("page-indicator")).toHaveTextContent("Page 2 of 2"); 
        expect(screen.getByTestId('sampleTestId-cell-row-10-col-col1')).toBeInTheDocument(); // confirm element in the next page is displayed
        expect(screen.queryByTestId('sampleTestId-cell-row-9-col-col1')).not.toBeInTheDocument(); // confirm element from previousd page is not displayed
        fireEvent.click(screen.getByTestId("goto-previous-page-button"));
        expect(await screen.findByTestId("page-indicator")).toHaveTextContent("Page 1 of 2"); 
        expect(screen.queryByTestId('sampleTestId-cell-row-10-col-col1')).not.toBeInTheDocument();
        expect(screen.getByTestId('sampleTestId-cell-row-9-col-col1')).toBeInTheDocument(); 
    });
});

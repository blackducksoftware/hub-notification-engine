import React from 'react';
import TableHeader from 'page/distribution/fun/TableHeader';

const DistributionConfigurationTable = () => (
    <div>
        <h1 className="d-inline-flex col-sm-4">Distribution</h1>
        <div className="brian-description">Configure the Distribution Job for Alert to send updates.</div>
        <div className="descriptorBorder" />
        <table className="brian-table">
            <tr className="brian-row">
                <TableHeader>First</TableHeader>
                <TableHeader>Second</TableHeader>
                <TableHeader>Last</TableHeader>
            </tr>
            <tr className="brian-row">
                <td>Data</td>
                <td>Second</td>
                <td>End</td>
            </tr>
            <tr className="brian-row">
                <td>Last Row</td>
                <td>Last Second</td>
                <td>Last End</td>
            </tr>
        </table>
    </div>
);

DistributionConfigurationTable.propTypes = {
};

DistributionConfigurationTable.defaultProps = {
};

export default DistributionConfigurationTable;

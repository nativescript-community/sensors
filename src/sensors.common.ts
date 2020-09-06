import * as httpModuleDef from '@nativescript/core/http';

let debug = false;
export function setDebug(value: boolean) {
    debug = value;
}

export enum CLogTypes {
    info,
    warning,
    error,
}

export const CLog = (type: CLogTypes = 0, ...args) => {
    if (debug) {
        if (type === 0) {
            // Info
            console.log('[@nativescript-community/sensors]', ...args);
        } else if (type === 1) {
            // Warning
            console.warn('[@nativescript-community/sensors]', ...args);
        } else if (type === 2) {
            console.error('[@nativescript-community/sensors]', ...args);
        }
    }
};

/**
 *
 *
 * @export
 * @param pressure current pressure in hPa
 * @param airportPressure sea level pressure in hPa
 * @returns alitude in meters
 */
export function getAltitude(pressure: number, airportPressure: number) {
    return 44330 * (1 - Math.pow(pressure / airportPressure, 1 / 5.255));
}

interface AVWXResult {
    altimeter?: {
        value: number;
    };
    units: {
        altimeter: 'hPa' | 'inHg';
    };
    info: {
        icao: string;
        city: string;
        name: string;
        latitude: number;
        longitude: number;
        elevation_m: number;
    };
}

function handleAVWXResut(result: AVWXResult) {
    if (result.units.altimeter !== 'hPa') {
        result.altimeter.value *= 33.8638866667;
    }
    return {
        city: result.info.city,
        name: result.info.name,
        latitude: result.info.latitude,
        longitude: result.info.longitude,
        icao: result.info.icao,
        elevation: result.info.elevation_m,
        pressure: result.altimeter.value,
    };
}

export async function getAirportPressure(apiKey, airport: string) {
    const httpModule = await import('@nativescript/core/http');
    const result = await httpModule.getJSON<AVWXResult>({
        url: `https://avwx.rest/api/metar/${airport}?onfail=cache&options=info&format=json`,
        method: 'GET',
        headers: {
            Authorization: apiKey,
        },
    });
    // returned pressure is in inHg
    if (!result.altimeter) {
        throw new Error(`airport not found ${airport}`);
    }
    return handleAVWXResut(result);
}
export async function getAirportPressureAtLocation(apiKey, lat: number, lon: number) {
    const httpModule = await import('@nativescript/core/http');
    let result = await httpModule.getJSON<AVWXResult>({
        url: `https://avwx.rest/api/metar/${lat},${lon}?onfail=cache&options=info&format=json`,
        method: 'GET',
        headers: {
            Authorization: apiKey,
        },
    });
    result = (result as any).sample || result;
    console.log('getAirportPressureAtLocation', 'result', JSON.stringify(result));
    // returned pressure is in inHg
    if (!result.altimeter) {
        throw new Error(`could not find airport pressure for location ${lat},${lon}: ${JSON.stringify(result)}`);
    }
    return handleAVWXResut(result);
}
